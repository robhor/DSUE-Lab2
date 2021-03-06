package analytics.event;


/**
 * An event dealing with bids.
 */
public class BidEvent extends Event {
	private static final long serialVersionUID = -7312684815832826724L;

	private final String userName;
	private final long auctionID;
	private final double price;

	/**
	 * Constructor.
	 * @param type Event type string
	 * @param timestamp Time of event occurence
	 * @param userName Name of the bidder
	 * @param auctionID ID of the affected auction
	 * @param price Bidding amount
	 */
	public BidEvent(String type, long timestamp, String userName,
	                 long auctionID, double price) {
		super(type, timestamp);
		this.userName = userName;
		this.auctionID = auctionID;
		this.price = price;
	}
	
	/**
	 * @return the userName
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * @return the auctionID
	 */
	public long getAuctionID() {
		return auctionID;
	}
	
	/**
	 * @return the price
	 */
	public double getPrice() {
		return price;
	}
	
	/**
	 * Produces a string representation of the event.
	 * Used for printing in ManagementClient.
	 * @return The event string
	 */
	public String toString() {
		if ("BID_PLACED".equals(getType())) {
			return String.format("%s - user %s placed bid %s on auction %s", super.toString(), getUserName(), getPrice(), getAuctionID());	
		} else
		if ("BID_OVERBID".equals(getType())) {
			return String.format("%s - user %s overbid with %s on auction %s", super.toString(), getUserName(), getPrice(), getAuctionID());	
		} else
		if ("BID_WON".equals(getType())) {
			return String.format("%s - user %s won auction %s with %s", super.toString(), getUserName(), getAuctionID(), getPrice());	
		}
		else {
			return super.toString(); // fallback for invalid type
		}
	}
}
