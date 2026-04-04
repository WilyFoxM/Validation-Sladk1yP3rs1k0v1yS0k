package ru.wilyfox.client.protocol;

import java.util.List;

public record DwSellersPacket(List<DwSellerEntry> entries) {
}
