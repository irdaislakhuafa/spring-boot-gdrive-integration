package com.irdaislakhuafa.dev.springbootgdriveintegration.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.irdaislakhuafa.dev.springbootgdriveintegration.models.Card;

import org.springframework.stereotype.Service;

@Service
public class CardService {
    private static Set<Card> listCards = new HashSet<>();

    public void addCard(Card card) {
        listCards.add(card);
    }

    public Set<Card> getCards() {
        return listCards;
    }

    public void generateDefaultCards() {
        listCards.add(
                new Card("service-1.svg", "Create", "You can create file in Google Drive using this option",
                        "/crud/create"));
        listCards.add(
                new Card("service-3.svg", "Read", "You can read file in Google Drive using this option",
                        "/crud/read"));
        listCards.add(
                new Card("service-2.svg", "Update", "You can update file in Google Drive using this option",
                        "/crud/update"));
        listCards.add(
                new Card("service-3.svg", "Delete", "You can delete file in Google Drive using this option",
                        "/crud/delete"));
    }
}
