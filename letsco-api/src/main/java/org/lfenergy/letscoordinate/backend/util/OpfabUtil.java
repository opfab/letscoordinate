package org.lfenergy.letscoordinate.backend.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpfabUtil {

    public static String generateProcessKey(EventMessageDto eventMessageDto, boolean toLowerCaseIdentifier) {
        String source = eventMessageDto.getHeader().getSource();
        String messageTypeName = eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().getMessageTypeName();
        if (toLowerCaseIdentifier) {
            return new StringBuilder().append(StringUtil.toLowercaseIdentifier(source))
                    .append("_")
                    .append(StringUtil.toLowercaseIdentifier(messageTypeName))
                    .toString();
        } else {
            return source + "_" + messageTypeName;
        }
    }
}
