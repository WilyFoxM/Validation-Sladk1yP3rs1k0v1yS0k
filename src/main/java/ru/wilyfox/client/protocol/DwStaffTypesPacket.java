package ru.wilyfox.client.protocol;

import java.util.Map;

public record DwStaffTypesPacket(Map<Integer, DwStaffType> types) {
}
