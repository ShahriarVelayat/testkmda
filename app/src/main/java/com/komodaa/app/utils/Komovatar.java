package com.komodaa.app.utils;

import android.util.SparseIntArray;

import com.komodaa.app.R;

import java.util.Random;

/**
 * Created by nevercom on 3/17/17.
 */

public class Komovatar {
    private static int[] avatars = {
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6,
            R.drawable.avatar_7,
            R.drawable.avatar_8,
            R.drawable.avatar_9,
            R.drawable.avatar_10,
            R.drawable.avatar_11,
            R.drawable.avatar_12,
            R.drawable.avatar_13,
            R.drawable.avatar_14,
            R.drawable.avatar_15,
            R.drawable.avatar_16,
            R.drawable.avatar_17,
            R.drawable.avatar_18,
            R.drawable.avatar_19,
            R.drawable.avatar_20
    };
    private int index = 0;
    private SparseIntArray avatarCache = new SparseIntArray(avatars.length);

    public int getNextAvatar() {
        if (index >= avatars.length) {
            index = 0;
        }
        return avatars[index++];
    }

    public Komovatar() {
        shuffleArray(avatars);
    }

    private static void shuffleArray(int[] array) {
        int index, temp;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            temp = array[index];
            array[index] = array[i];
            array[i] = temp;
        }
    }

    public static int getRandomAvatar() {
        return avatars[rand()];
    }

    private static int rand() {
        return (int) (Math.random() * (avatars.length - 1));
    }

    public int getOrderedAvatar(int position) {
        int avatar = avatarCache.get(position);
        if (avatar > 0) {
            return avatar;
        }
        int nextAvatar = getNextAvatar();
        avatarCache.put(position, nextAvatar);
        return nextAvatar;
    }
}
