package com.example.paceexchange;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class InventoryData {

    private String category, itemID, title, url, tradeInFor, tag;
    private FirebaseFirestore mFirestoreDatabase;


    public InventoryData(String category, String title, String tradeInFor, String url, String tag) {

        mFirestoreDatabase = FirebaseFirestore.getInstance();
        itemID=FirebaseDatabase.getInstance().getReference().push().getKey();
        this.category=category;

        this.title=title;
        this.tradeInFor=tradeInFor;
        this.url = url;
        this.tag = tag;
    }

    public InventoryData(String category, String title, String tradeInFor, String itemID, String url, String tag) {

        this.category=category;
        this.itemID=itemID;
        this.title=title;
        this.tradeInFor=tradeInFor;
        this.url=url;
        this.tag = tag;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

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

    @Override
    public String toString() {
        return "InventoryData{" +
                "category='" + category + '\'' +
                ", itemID='" + itemID + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", tradeInFor='" + tradeInFor + '\'' +
                ", tag='" + tag + '\'' +
                ", mFirestoreDatabase=" + mFirestoreDatabase +
                '}';
    }
}
