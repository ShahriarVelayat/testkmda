package com.komodaa.app.interfaces;

/**
 * Created by nevercom on 10/15/16.
 */

public interface CommentListener {
    void commentPosted();

    void replyTo(long id, String userName);
}
