package com.next.common;

import com.next.model.TrainUser;

/**
 * @ClassName : RequestHolder
 * @Description :
 * @Author : NathenYang
 * @Date: 2020-09-05 22:53
 */

public class RequestHolder {

    public static final ThreadLocal<TrainUser> userHolder = new ThreadLocal<>();


    public static void add(TrainUser trainUser) {
        userHolder.set(trainUser);
    }

    public static TrainUser getCurrentUser() {
        return userHolder.get();
    }

    public static void remove() {
        userHolder.remove();
    }
}
