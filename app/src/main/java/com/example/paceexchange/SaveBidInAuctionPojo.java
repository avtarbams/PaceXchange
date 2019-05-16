package com.example.paceexchange;

public class SaveBidInAuctionPojo {

    public SaveBidInAuctionPojo(String category, String itemID, String title, String url, String tradeInFor, String tag, String username) {
        this.category = category;
        this.itemID = itemID;
        this.title = title;
        this.url = url;
        this.tradeInFor = tradeInFor;
        this.tag = tag;
        this.username = username;
    }

    @Override
    public String toString() {
        return "SaveBidInAuctionPojo{" +
                "category='" + category + '\'' +
                ", itemID='" + itemID + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", tradeInFor='" + tradeInFor + '\'' +
                ", tag='" + tag + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

    private String category;
    private String itemID;
    private String title;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTradeInFor() {
        return tradeInFor;
    }

    public void setTradeInFor(String tradeInFor) {
        this.tradeInFor = tradeInFor;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String url;
    private String tradeInFor;
    private String tag;
    private String username;

}
