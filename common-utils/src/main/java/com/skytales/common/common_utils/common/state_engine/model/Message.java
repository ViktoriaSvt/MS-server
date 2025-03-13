package com.skytales.common.common_utils.common.state_engine.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.skytales.common.common_utils.common.state_engine.dto.BookMessage;


public class Message {

    private UpdateType updateType;
    private BookMessage book;

    @JsonCreator
    public Message(
            @JsonProperty("updateType") UpdateType updateType,
            @JsonProperty("book") BookMessage book) {
        this.updateType = updateType;
        this.book = book;
    }

    public UpdateType getUpdateType() {
        return updateType;
    }

    public BookMessage getBook() {
        return book;
    }

    public void setUpdateType(UpdateType updateType) {
        this.updateType = updateType;
    }

    public void setBook(BookMessage book) {
        this.book = book;
    }

    public Message() {
    }
}
