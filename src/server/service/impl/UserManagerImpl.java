package server.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import analytics.event.Event;
import analytics.event.UserEvent;

import server.bean.Client;
import server.bean.User;
import server.service.ClientManager;
import server.service.UserManager;

public class UserManagerImpl implements UserManager {
	private ClientManager clManager;
	private AnalyticsServerWrapper analyticsServer;
	
	private ConcurrentHashMap<String, User> users;
	private HashMap<User, ArrayList<String>> pendingNotifications;
	
	public UserManagerImpl(ClientManager clManager, AnalyticsServerWrapper analyticsServer) {
		this.clManager = clManager;
		this.analyticsServer = analyticsServer;
		users = new ConcurrentHashMap<String, User>();
		pendingNotifications = new HashMap<User, ArrayList<String>>();
	}

	@Override
	public void disconnect(User user) {
		Client client = user.getClient();
		if (client != null) clManager.disconnect(client);
		user.setClient(null);

		// notify analytics
		Event event = new UserEvent("USER_DISCONNECTED", System.currentTimeMillis(), user.getName());
		analyticsServer.processEvent(event);
	}

	@Override
	public void logout(User user) {
		if (user != null) {
			user.setClient(null);

			// notify analytics
			Event event = new UserEvent("USER_LOGOUT", System.currentTimeMillis(), user.getName());
			analyticsServer.processEvent(event);
		}
	}

	@Override
	public User login(String username, Client client) {
		if (username == null) throw new IllegalArgumentException("Username can't be null!");
		if (client   == null) throw new IllegalArgumentException("Client can't be null!");
		
		// check if already registered user
		User user = users.get(username);
		if (user != null) {
			if (user.getClient() != null) return null; // Can't log in on two machines
		} else {
			user = new User();
			user.setName(username);
		}
		
		user.setClient(client);
		users.put(username, user);

		// notify analytics
		Event event = new UserEvent("USER_LOGIN", System.currentTimeMillis(), user.getName());
		analyticsServer.processEvent(event);
		
		sendQueuedNotifications(user);
		
		return user;
	}

	@Override
	public void postMessage(User user, String message) {
		Client client = user.getClient();
		if (client != null) {
			clManager.postMessage(user.getClient(), message);
		} else {
			addNotificationToQueue(user, message);
		}
	}
	
	private void addNotificationToQueue(User user, String message) {
		synchronized (pendingNotifications) {
			ArrayList<String> notifications = pendingNotifications.get(user);
			if (notifications == null) {
				notifications = new ArrayList<String>();
				pendingNotifications.put(user, notifications);
			}
			notifications.add(message);
		}
	}

	private void sendQueuedNotifications(User user) {
		synchronized (pendingNotifications) {
			ArrayList<String> notifications = pendingNotifications.get(user);
			if (notifications == null) return;
			
			for (String msg : notifications) {
				postMessage(user, msg);
			}
			notifications.clear();
		}
	}

	@Override
	public Collection<User> getUsers() {
		return users.values();
	}

	@Override
	public void sendMessage(User user, String message) {
		if (user == null) throw new IllegalArgumentException("User can't be null!");
		
		Client c = user.getClient();
		if (c == null) return;
		clManager.sendMessage(c, message);
	}

}
