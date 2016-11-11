package com.javic.pokewhere.models;

import POGOProtos.Inventory.Item.ItemIdOuterClass;

/**
 * Created by vagprogrammer on 11/11/16.
 */

public class ItemToDelete {

    private ItemIdOuterClass.ItemId mChildItemId;
    private int count;


    public ItemToDelete(ItemIdOuterClass.ItemId mChildItemId, int count) {
        this.mChildItemId = mChildItemId;
        this.count = count;
    }

    public ItemIdOuterClass.ItemId getmChildItemId() {
        return mChildItemId;
    }

    public void setmChildItemId(ItemIdOuterClass.ItemId mChildItemId) {
        this.mChildItemId = mChildItemId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
