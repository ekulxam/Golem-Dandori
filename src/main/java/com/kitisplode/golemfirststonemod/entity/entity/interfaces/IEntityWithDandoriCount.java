package com.kitisplode.golemfirststonemod.entity.entity.interfaces;

import com.kitisplode.golemfirststonemod.util.DataDandoriCount;

public interface IEntityWithDandoriCount
{
    void recountDandori();
    void setRecountDandori();

    int getTotalDandoriCount();
    int getDandoriCountBlue();
    int getDandoriCountRed();
    int getDandoriCountYellow();
    int getDandoriCountIron();
    int getDandoriCountSnow();
    int getDandoriCountCobble();
    int getDandoriCountPlank();
    int getDandoriCountFirstStone();
    int getDandoriCountFirstOak();
    int getDandoriCountFirstBrick();
    int getDandoriCountFirstDiorite();

    void nextDandoriCurrentType();
    DataDandoriCount.FOLLOWER_TYPE getDandoriCurrentType();
}
