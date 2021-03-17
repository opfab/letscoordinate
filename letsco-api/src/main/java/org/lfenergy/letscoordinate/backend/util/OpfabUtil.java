package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpfabUtil {

    public static String generateProcess(EventMessageDto eventMessageDto) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName =
                eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        return source + "_" + messageTypeName;
    }
}
