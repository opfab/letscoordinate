/*
 * Copyright (c) 2020-2021, RTE (https://www.rte-france.com)
 * Copyright (c) 2020-2021 RTE international (https://www.rte-international.com)
 * See AUTHORS.txt
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 * This file is part of the Let’s Coordinate project.
 */

package org.lfenergy.letscoordinate.backend.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.lfenergy.letscoordinate.backend.dto.KafkaFileWrapperDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.EventMessageDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDetailsDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieDataDto;
import org.lfenergy.letscoordinate.backend.dto.eventmessage.payload.TimeserieTemporalDataDto;
import org.lfenergy.letscoordinate.backend.enums.CoordinationEntityRaResponseEnum;
import org.lfenergy.letscoordinate.backend.enums.CoordinationStatusEnum;
import org.lfenergy.letscoordinate.backend.enums.FileDirectionEnum;
import org.lfenergy.letscoordinate.backend.enums.FileTypeEnum;
import org.lfenergy.letscoordinate.backend.model.*;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lfenergy.letscoordinate.backend.util.Constants.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CoordinationFactory {

    public static final String KAFKA_JSON_DATA = "{\n" +
            "  \"fileName\": \"CoordinationA.json\",\n" +
            "  \"fileType\": \"JSON\",\n" +
            "  \"fileContent\": \"ew0KICAiZXZlbnRNZXNzYWdlIjogew0KICAgICJ4bWxucyI6ICJodHRwOi8vaWVjLmNoL1RDNTcvMjAxMS9zY2hlbWEvbWVzc2FnZSIsDQogICAgImhlYWRlciI6IHsNCiAgICAgICJ2ZXJiIjogImNyZWF0ZWQiLA0KICAgICAgIm5vdW4iOiAiQ29vcmRpbmF0aW9uIiwNCiAgICAgICJ0aW1lc3RhbXAiOiAiMjAyMS0wNy0yMlQxNDoyNToyOFoiLA0KICAgICAgInNvdXJjZSI6ICJTZXJ2aWNlIEEiLA0KICAgICAgIm1lc3NhZ2VJZCI6ICIzZjVkMGE3YTZlOTI0MmIyMzg5M2RkNTEwMzQ0MzE4NTUiLA0KICAgICAgInByb3BlcnRpZXMiOiB7DQogICAgICAgICJmb3JtYXQiOiAiSlNPTiIsDQogICAgICAgICJidXNpbmVzc0RhdGFJZGVudGlmaWVyIjogew0KICAgICAgICAgICJidXNpbmVzc0FwcGxpY2F0aW9uIjogIlBhbkV1cm9wZWFuU2VydmljZUFUb29sIiwNCiAgICAgICAgICAibWVzc2FnZVR5cGVOYW1lIjogIkNvb3JkaW5hdGlvbiBBIiwNCiAgICAgICAgICAiYnVzaW5lc3NEYXlGcm9tIjogIjIwMjEtMDctMThUMjI6MDA6MDBaIiwNCiAgICAgICAgICAiYnVzaW5lc3NEYXlUbyI6ICIyMDIxLTA3LTI0VDIyOjAwOjAwWiIsDQogICAgICAgICAgInNlbmRpbmdVc2VyIjogIjIyWENPUkVTTy0tLS0tLVMiLA0KICAgICAgICAgICJyZWNpcGllbnRzIjogWyIxMFhGUi1SVEUtLS0tLS1RIiwgIjEwWDEwMDFBMTAwMUEzNDUiXQ0KICAgICAgICB9DQogICAgICB9DQogICAgfSwNCiAgICAicGF5bG9hZCI6IHsNCiAgICAgICJ0aW1lc2VyaWUiOiBbDQogICAgICAgIHsNCiAgICAgICAgICAibmFtZSI6ICJDb29yZGluYXRpb24iLA0KICAgICAgICAgICJkYXRhIjogWw0KICAgICAgICAgICAgew0KICAgICAgICAgICAgICAidGltZXN0YW1wIjogIjIwMjEtMDctMDlUMDE6MzA6MDBaIiwNCiAgICAgICAgICAgICAgImRldGFpbCI6IFsNCiAgICAgICAgICAgICAgICB7DQogICAgICAgICAgICAgICAgICAibGFiZWwiOiAiRXZlbnQiLA0KICAgICAgICAgICAgICAgICAgInZhbHVlIjogIkV2ZW50IEEiDQogICAgICAgICAgICAgICAgfSwNCiAgICAgICAgICAgICAgICB7DQogICAgICAgICAgICAgICAgICAibGFiZWwiOiAiQ29uc3RyYWludCIsDQogICAgICAgICAgICAgICAgICAidmFsdWUiOiAiQ29uc3RyYWludCBBIg0KICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgew0KICAgICAgICAgICAgICAgICAgImxhYmVsIjogIlJlbWVkaWFsIGFjdGlvbnMiLA0KICAgICAgICAgICAgICAgICAgInZhbHVlIjogIkFjdGlvbiBBIg0KICAgICAgICAgICAgICAgIH0NCiAgICAgICAgICAgICAgXQ0KICAgICAgICAgICAgfSwNCiAgICAgICAgICAgIHsNCiAgICAgICAgICAgICAgInRpbWVzdGFtcCI6ICIyMDIxLTA3LTA5VDAxOjMwOjAwWiIsDQogICAgICAgICAgICAgICJkZXRhaWwiOiBbDQogICAgICAgICAgICAgICAgew0KICAgICAgICAgICAgICAgICAgImxhYmVsIjogIkV2ZW50IiwNCiAgICAgICAgICAgICAgICAgICJ2YWx1ZSI6ICJFdmVudCBCIg0KICAgICAgICAgICAgICAgIH0sDQogICAgICAgICAgICAgICAgew0KICAgICAgICAgICAgICAgICAgImxhYmVsIjogIkNvbnN0cmFpbnQiLA0KICAgICAgICAgICAgICAgICAgInZhbHVlIjogIkNvbnN0cmFpbnQgQiINCiAgICAgICAgICAgICAgICB9LA0KICAgICAgICAgICAgICAgIHsNCiAgICAgICAgICAgICAgICAgICJsYWJlbCI6ICJSZW1lZGlhbCBhY3Rpb25zIiwNCiAgICAgICAgICAgICAgICAgICJ2YWx1ZSI6ICJBY3Rpb24gQiINCiAgICAgICAgICAgICAgICB9DQogICAgICAgICAgICAgIF0NCiAgICAgICAgICAgIH0NCiAgICAgICAgICBdDQogICAgICAgIH0NCiAgICAgIF0NCiAgICB9DQogIH0NCn0NCg==\"\n" +
            "}";
    public static final String KAFKA_EXCEL_DATA = "{\n" +
            "  \"fileName\": \"CoordinationA.xlsx\",\n" +
            "  \"fileType\": \"EXCEL\",\n" +
            "  \"fileContent\": \"UEsDBBQACAgIADc35lIAAAAAAAAAAAAAAAAYAAAAeGwvZHJhd2luZ3MvZHJhd2luZzEueG1snZDNDoIwEITvPgXZuxQ9GEP4uRCfQB9gQxdoQrdNtwq+vU2Qu/E4mcyXmana1c7Zi4IYxzWc8gIy4t5pw2MNj/vteIVMIrLG2THV8CaBtjlUqw7lIl3IUp6lTLKGKUZfKiX9RBYld544uYMLFmOSYVQ64JLIdlbnorgo8YFQy0QUu82BLw//oFk0vOd/auOGwfTUuf5pieMGCTRjTF/IZLxAU6l9aPMBUEsHCMfNTquoAAAAKwEAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAGgAAAHhsL19yZWxzL3dvcmtib29rLnhtbC5yZWxzvZPBTsMwDIbve4rId5q2GwihprsgpF3HeIAodZtqbRLZAba3XxAIOmmqOEw7Wbbj//8Of6r1YRzEBxL33ikoshwEOuOb3nUK3nYvd4+wrhfVFgcd0xO2fWCRbhwrsDGGJynZWBw1Zz6gS5vW06hjaqmTQZu97lCWef4gaaoB9Zmm2DQKaNMUIHbHgP/R9m3bG3z25n1EFy9YSI7HATkpauowKvjus6QD8rJ9eU37T097tojxj+B3lOC+SjEHs7wxTDkHs7oxzHIO5v6qKbGasHmNlCI/Dct0/AOzqOTZR6hPUEsHCHIpWCzhAAAAPwMAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAFAAAAHhsL3NoYXJlZFN0cmluZ3MueG1snVddb9owFH3fr4jynsYJtHwMqFigEmpHN0g1tJfJOIZaTezMdlD59zMUWp6m4yIEAs65vvfcLzO4fa3KYMe1EUoOw+SKhAGXTBVCbofhU34XdcPAWCoLWirJh+Gem/B29GVgjA0cVZph+Gxt3Y9jw555Rc2Vqrl0v2yUrqh1H/U2NrXmtDDPnNuqjFNCbuKKChkGTDXSumN77TBopPjb8Oz0TdIKRwMjRoPjMX1TU+ZOd3YM1zsejpzL60FsR4P4APoPUDqDENCKirtIqxpCG9VoxiGoM2vols8KCP2mGwRdN0ZIZ3xc16Vg1LoU+jiU72uvAA74Oa0wztm3Cd3faVX5cnIFMWqtmCMsLceydsjxRqMxvKPnTbXmGqsLLg+982RAvOZM1IJLayA4c41kOVZJmVLa+YLXRUrSJCKdKE3zpN1Pr/tp9zdEXLo3wXgwxtJMOgml14T0Opt2b5Osi5se76aUdAjpttc3kJHVw3IFAX9QOW20G0pUntwc50qV3gqCwZ01TLp5mvYJcU9Mw3fx257E2XyWz8YPfxZPcwj/C3MnXWWPi+nyMTo+lhApIau7RbTIp2+kn1/dFwkhyfj40mpfQ1YKaik8myTazAJrmpKuOVYcXLBMFdjhW01lU1It7B7C72jZgDOKv2LLgqmqcmMGws5VYGo3mDaCBdotdf1iAreXAqsKigXAlGRlY9DJ83jvaf+4rLkWmEjTHRr5uQtJLydJv4V34fEIcEZkShqr3QUI8+kDDtpf8IoXgpYBZYfJhW2WMfvMlCOfkumbb9wY4RQCBs7RxvmOVTA2XYR8wa4Fht3XAgvE697qRLVi6674WJ89KHq4zmCbdgGWP4hz9gKf8zNPvCtiEpFWRJL8uG7hIj5qKI8iRolnKYOEUxwg2imFI/1Np76mMcKljDDjLCNG8HPoEGvLN1aYELV9TWOESxkxZy5kxAj+sWI3vYtYMUIGF3rmW+iXMsLCn2XECJ55hTsv8+48uNAz30K/lBHM6YeMGMG/ZGDB/TMEO+Hn9eVu6vrspuEE8304BXET7P/4cAriJl3QHoibJAQ0iAInCdZFzuIFMDbGjv4BUEsHCJ5kMREzAwAATxUAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAIwAAAHhsL3dvcmtzaGVldHMvX3JlbHMvc2hlZXQxLnhtbC5yZWxzjY/LCsIwEEX3fkWYvUnrQkSadiNCt1I/ICTTNNg8SOLr781GseDC5cy9c4bTdA87kxvGZLzjUNMKCDrplXGaw3k4rnfQtavmhLPIpZImExIpNy5xmHIOe8aSnNCKRH1AV5LRRytyGaNmQciL0Mg2VbVl8ZsB7YJJesUh9qoGMjwD/sP242gkHry8WnT5xwumorgXj4IUUWPmQOl79wlrWrDAiiJbOLYvUEsHCMLVMZeoAAAAGgEAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAGAAAAHhsL3dvcmtzaGVldHMvc2hlZXQzLnhtbL1aXXOjOBZ9319B8b42kjFgyvHUtDFOu3o6U5uenap9I0aOqcaIBZxM96/fK76Mda+3/JAhDwk+90ucKwmOo+Uvf51S400UZSKzB5NNLNMQ2V7GSfb6YP7xLfynZxplFWVxlMpMPJg/RGn+svrH8l0W38ujEJUBCbLywTxWVe5Pp+X+KE5ROZG5yMBykMUpquBj8Tot80JEcR10SqfcspzpKUoys8ngF/fkkIdDsheB3J9PIquaJIVIowqGXx6TvDRXy7rC74VxSNJKFL/JGIZ9iNJSgC2PXsWzqP7Ia3v1Tf4OQGeerpbTNni1jBOooFgxCnF4MD9xf8dnyqX2+Hci3svBtVEe5XsIAz2nUdnlq8FtkcRfkkwAWhXnFvyXfF/L9BHoAOaHhv+IQvZAkbweYYhfxKHqU1bRy7NIxb4S8VWZp3OVQpXnH6cXmfYZYnGIzmmlxgD1ZNHhbzDkBzNTxKaQU+aqxlqk6YP5KzONvfL9DAUc2zR+Snl63kcp0MQsa/D5ax2uo4rQL9EPea55aa1qtrxI+V1BKq+l2lTfhiI4j9TMakdhGhGgb6IZzVc2HwJNrFH+t+6JMvY9U6mH1113wnr2QLtbLoD6R6GIhaHxiQvpfwLnHdQyKhs2v4g3kUJAXXSIAZnNTUyvaqyWwFxZ/1YcplFeDtq0P5eVPP2ZxNWx68MxiWORkWXrmqfoLxgm/E2y+m9Z/VB9UIw2afjE5oqDj63I24qcqMgWE5d9fEm7LTmjSvKJ53x8yXlbck6VtCbe/ONLOm1Jhyi5mLDZx1d024ouUdH5WybPoq3o0Z38OyYPs7olYo03fZjFuxnEqKXZ9XPa7An1RhFEVbRaFvLdKOrV1dRuto++XL01zSZzNI7G+/9sVXV5dIcwBKjYl52NVnavyn1SKxqqwrSAaPV8flvN+HL6pphpfdaNj20NfOaLa5+A8HGsa58N4WM71z4hlYdd+2wpH23Mj5TP7NrnM+VjX/vsKJ9573PVOnvk1tnD1mX10DSm1jburqMxHjQ+Nhv6uFrnKB9P6xxVS5sl2zYPH/i42ix5bH2a3bbuExWl3eluGHXVlfnIXZl3NHTDXyMkmHd0dsgGISGK2rY+9pAGfdq3PvMLeVSUthB2w6gr8pyRyXMQeQgJGsR2hjekrdoN5TPX5quD56urrY2tQ5CnrY1HB1FORWmrZefcotwdmXIXUY6QwO3o7OcrQkIUtXUJGrQN4dFF5BFRnrZF7Nxb5Hkjk+ch8hASNIjtDm9I2702lI+2tEMPz1dPW8hbjyBPWxuPHqKcitJWy867RfliZMoXiHKEBIuOzn6+IiREUduFTszjgiBG2yI+o6gdFeXSrwzqjXhU9upX8Gv6MBS0kO0Nb0HbwTakk7a+w0vyi9NCW87bLtOA+Q4akrjQVs1nHLcj4/gN8tnY5DNMPoKCFgJe+9mLoRAHbjuvIY2MoEN/DcZxOzLOvkHjeEqppZFjGhEUtJB9NfPm+hymnHRJckk+cHL1Ocwx+Zwg0dPJR3E7Mm5xg/yx9SKbYfIRFLSQvRjMYQSFOHDbeQ1pnGE6mGXpPKLAHR3IbhA5tnpjNiaSkFDM0t4DAkaIMWbN9JlNetn61L5HtHWprl7kdNXWOQ1eK8g4XbddxV13ZGzlxrB0w1DAsHjDUIgDt+we/cawgCPjdAXHbko4NraGY1jEYShghERjFtqfSS+0Qd8j5dg9Wo5hMUfG6WqO3ZRzbGw9x7Cgw1DAsKTDUIgDt+weVcewrKPikK5jN4UdG1vZMSztMBQwQrgxS//2jPbSvz9j9wg8do/CY1jikXG6xmM3RR4bW+UxLPMwFDAs9DAU4sAtw1qP3SP2cNyOjLsh9/jYco9juYehgBNKjlnaot6QXkxbwiG/R/BxLPj4PYIPx+3IuBuCj48t+DgWfBgKOBZ8GApx4JZjwcfvEXw4bkfG3RB8fGzBx7Hgw1DACS3HmP5NG+2lf9fG75F8HEs+fo/kw3E7Mu6G5ONjSz6OJR+GAo4lH4ZCHLjlWPLxuyQfDtzRgbrkmw7+23sSxWt9eKUEAs4ZRM1m5gBuzxfZ/qfmfJFmWNv+mlGGwPaDOYFvbH9D4aHthxQeOH7gUnkcf0PhoeOHFB54frCg8nj+hsJDzw8pPGCWr76iIjKBZUNaQrCEpCVg3A9I/jZg2ZCWECwhaQEB7a/JNoFo9gNG9gMsG9IC4tgPSQuIF1+9QVPZoC2kBUSKH5IWeIn01ZsMlQ2aQ1rgZdEPSQs8zH31RCGygWVDWuCh7YekBTZVPyAZhY3U35AW2Dz9sLFML6trtcyLJKue8vosoHEUkTrEWPbb0evlIJ6OPIuqP/ohi+SnzKooXYusEsXgoMibKKpkjw3T5ljhb1HxmkDhtD6uZ03c9gBfe13JvL6CzfNFVrBbdp+O9SlA9WnOmGfN+x/TOEhZ0aZpf5TxnBt5lIviOfkp6v+VlIOzeoekKCt1Iu/r+fRSZzKbY4/tcRfWfuxPxJmGSvtU1COK5Xv27SiyJ7hz2M6LBG68Pmn5YOayqIooqeBm0mj//dcs/vOYVP1JSiMuosGhxT30Zy1P6shmqY4dZoCdSxHqo+seM30PgjyB7VLdSUf+BdnLPFHNrGdVQ2JY82XEyeEADcqqusBlTB38FMebt8vTa7WUcdycxYQJNbiGyyZjA/fXw2LwsT8Ku/ofUEsHCNv+TOLrBwAATisAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAGAAAAHhsL3dvcmtzaGVldHMvc2hlZXQxLnhtbL2aS3PiOBDH7/spXBz2tAt+YsgSthISQjK8IVC1NweL4BpjeW2TTPLpV5Lll1pJUVtx5jCBn9vdLfXfLQHq/f3r6CsvKIo9HFw2tKbaUFCww64XPF82HtfDPzsNJU6cwHV8HKDLxhuKG3/3f+u94uhnfEAoUYiDIL5sHJIkvGi14t0BHZ24iUMUkCt7HB2dhLyNnltxGCHHZTcd/Zauqu3W0fGCRurhIjrHB97vvR26wbvTEQVJ6iRCvpOQ9OODF8aNfo9FmEfK3vMTFE2wS9LeO36MyLXQeUYrlDyG7HqyxnMCssutfq/Fb+73XI9EoLOiRGh/2bjSLq4mD5pOjZjNxkOvcem1Eh/w65CkevKdOPPI4F3kuWMvQIQm0YnDJX4dYH9EJoTMffnCPyjCOYi85wNJcoz2Se4ycZ5WyEe7BLnl+2anxCdBVm/HJ+znDly0d05+QlMg4XCU8ReS8WUjoDPrE5c4pCEGyPcvGw9aQ9lR23viv202lHeMj6ud45N50lS19H7KbhcpndGx84ZPbFr4VSqXJ4x/UkT9qqwWAVJ+rULfSyhQ3vhLU0joymoozi7xXohrKsEnnCT4SK8zaSa0fhF+RwGrDZsaWraQGXNPmYdiiMX7NB8l/pcVmlyTuCnHLHua6x97muuFoui4y68z5QyZtokYeaGILEaIFp3Mm94kz947kUNGuABwWukxekE+sWchy4wUOp3gViVEv0eqGrP/aX19J4ypgrjT3SkmA9x6bnLINHLwXBcF0rAs5tH5RbIkf72A/Y2TN6oRWu3UjWY0TYPOwdeG1HlIXRbSalrtrw9p8JBGHlIrQnabtvb1IU0e0pSFbDf1GkZp8ZCWJKShN03960O2eci2rJZ2s1vDKG0e0paE1PVaQnZ4yI5kYvV2U6vhIenykF1ZSKOWWmpq1gtUWVCtadk1BM0bkCZ/NmsZadaCNF3+dNYy0qwJadIuZDY7NShXy9qQJutD5HEh/OuDZo1Ik3UiUtNagmatSGtLglpWs46SZr1Is+UDNWpYWLSsG2mydqRpatOooR9pWUPSZB3Jsps1jFTPGpIua0g19QY9a0i6tCEZTbWOoPmeSNaQdKuehqTmzVc3JItpp9muJWo+wyQB2R6we/6Cytm5gfO+pOrSzSdZ4tI9f7rTZtvvGydx+r0IvyoRq0kaP92UFwnk+30hldT6kw8ALD4YJRk8DXdFJcg2yuRm+tH4pa/2Wi80QW5xDS20qsUAWuhVixtoYVQtblMLo2RhVi2G0MKqWtxBi3bVYgQt7KrFPbToVC0eoEW3avEDWmjCpI5TE7NsIszqRGIiTOtUYiLM60xiIkzsXGIizOxjaqKyZ4aCTQm0iHJz+erfLF+d5WGVcxdqfi0xEYo+SE3aZROh6jcSE6Hst6mJXX4MhLIPYS66UPY7iYlQ9lE5UMD1Jbi5lyQjSONBYiJI44fERJDGWGIiVGAiMREqMIVDEnOZSbwIRZpLTIQiPeqikvUPlGx8s5INlkcnT+xaBIMUdHNwI4LbFGjF6IaikzsRjMA994A8APIDkDEgE0CmgMwAmQPyaIglMz4omZne2hUrx/cONRWOR9XKi5u4hspsxFWU21QWSXEd5TblpcUQV1JuU+7ngpshNyk3GUN43O5kNsKzP5LZCA//PbcpxCSCHyIYc9AupATIFJAZJ3YhJUAWgCwBWQGyBuQRkA0gW06KR+1qwidMLaF7iB7MD/RtfXNLstJdm1our9C4r7lNRdlCWx7kNnnnAuQWkCEgd4CMOKk8M0LHH1tATYBMAZkBMgdkAcgSkBUga0AeAdkAsuUjNYAo2t8sinaaiPmZKNpZ+qXdvNASB21xiDeA3AIyBOQOkBHPsNyVTKHZjsFdE0CmgMwAmQOyAGQJyAqQNSCPgGwA2bY/EoX9zaKwYacwhUXn2oadAmzEKzaB7MPmjcSNKS6B3KaiP/HjJLcpGvYdICMbthdT3P/aQEmATAGZATIHZAHIEpAVIGuec7HsPHJS7BM3gGztj5TU+WYldWB7AUrqwPYClFSxkStJ4sYUdi+3nWw2P1FSR5zNO0BGHUlPElIed4CSAJkCMgNkDsgCkCUgK0DWHaAkMK4NINvOR0oC2/KaldRliRif9iRu82lPqtjIlSRxYwqboFtuo3+mJG5jFEoCZMRJ5QERtzxdoCRApoDMAJkDsgBkCcgKkHUXKKkLlATItvuRkuivh9/7Jal6RlfiRp+3papRIP+yVOIILHFZSuXOZIlfN2VGpdYE0ShD5eZkiRumIqlcUxBNIZpBNIdoAdESohVE62Iecm3BIW4g2mZIIq9v/w5eO0de2jny0s6Ql8QRWPeylD6XlwblBdAoQxV56aK8wH0TiKYQzYrRFPICaAHREqIVROtiHgp5gSQ2EG0zJJHXd39HrunnyEs/R176GfKSOAKLYZbS5/LSobwAGmWoIi9DlJcuVnYC0RSiGURziBYQLSFaQbQu5qGQFxjiBqJthkryapV+THROCR6yw638bKp5MTLpeb/iQr8XRl6QzEJ2MFY5IIee6I1zfT0XZ1JFskLFl6o48t5xkDj+AAXEa+ln1BcUJd4OXmilZ2wnTvTskcA+O7mqNu2ObfHjrMXbBIeX9Mixpdv5P6KZ9Kyl7MqBHZctHOwxTkrvW/n53lOohE6IopX3jtIzUKXzq3svihN6SnV6Oj7R27VGehaY/yicvc0PYjYU6nYWsdgufg3WBxTMyAyQ5zTyyASw48eXjRBHSeR4CRmC7+x+XgXu9uAl+fFixY2c0jneHfL9AT7Sc8wxPYobEHaK0VDMTizFTejRja9a1KAgOxx6tKbsS7R0toZsjhTX2+9JnYKE+S9SyvDMdW9fiq7U72HXTU8n9393juFfA/b/7/+ecPLX2juiWJmiV2WJj07wR3oOOL3GzDSd/bnqtQov1GGay/9ySGdEYa/nzCt31WuVR9nvkSl+JUJXoguPSDK6d9lMtPLT6v3/AFBLBwjH72orxwgAAPEuAABQSwMEFAAICAgANzfmUgAAAAAAAAAAAAAAABgAAAB4bC93b3Jrc2hlZXRzL3NoZWV0Mi54bWy9WF1vozgUfd9fgXiYp92QT8h2SEZt0iQrdZpq2tmR9s0BE6wCZmyTTPvr99oGAoSN0GomfWjD9eGe43Ociov76UccGQfMOKHJzBz0+qaBE4/6JNnPzK8vqz+mpsEFSnwU0QTPzDfMzU/z39wjZa88xFgY0CDhMzMUIr2xLO6FOEa8R1OcwEpAWYwEXLK9xVOGka9uiiNr2O/bVoxIYuoON6xLDxoExMNL6mUxToRuwnCEBMjnIUm5OXcVwxMzAhIJzD5TH2QHKOIY1lK0x89YfE3VunihT1Aolq25a+U3z12fAIN0xWA4mJm3g5vNVCIU4G+Cj7zy2eAhPa5AZxYhXrRTxTUj/gNJMFQFy/LiF3pc0GgDboDx1YV/MKNlgZF9CAofcCDKlgLtnnGEPYH9Gs02ExGwPL/FOxqVHXwcoCwSUgPwUVbUDyB5ZibS1wh60lRyLHAUyX2ahiexfwGBPTaNd0rjZw9F4NKg369cP6rbm1Xp5wN6o5nyJV+Vh2VH6assyb59mZLahvQ3RfJg5SpMA0H1gLWa9WBcLeh7Df5dRSIXy8hk6+rnIp2VOjyQdu4FWL/B0liQNuw5E5AOnhel3FGq3XzABxzBDYq0WgMz9SasGsfcBee4+i09jFDKKzF5GRc0/kZ8ERY5hMT3cdJKqzhj9GNmDuEvkd9N+UV8kzlIR3Wbac92pAc/l3GUM45aGAd2zxn8fMpxTjluo5z+EspJTjm5mq92zmi3+zr9BZSD/rDYp3Nhn5Y+uOo0L5FAc5fRo8HUodPU+oyXbJXvT0OHhl/4Qin+sx3CxiXfnTzlcPKgL9wt/5sf5qO+ax2kxhyzaMMM6phlG2ZYx9y3YUZ1zKoF02izbmszrmM2bRi7xFjgdmn58MqW3w61NruibdLQf5djnCpm0oilA2bZhrEbsXTArDpg1h0wm8uaa7mMrp3LqCUXp5HLqEMuHTDLNkwzlw6YVQfMuoOezWVMLZfxtXMZt+QybeQy7pBLB8yyDdPMpQNm1YFr3aHP5nKfWi6Ta+cyOc9l3PhffTfpkEsHzLIN08ylA2bVgWvdoc/mcp9aLvaVc7m3tTY1PekdVSs1bc61tTln2pz/0ja9trbpmbbpmTar8siWMpKIbapGYSOE4RJm+NMwuj8Nos0KDMTlQyVl5J0mAkULmK8xqzyCHjATxDtfsPRU/RmxPQHiSI2r/Z4zlY4wvffiEoY89ZJhMnTKH9jUjgowqm0lVDPyqUFAqahcW+VEn6UwR6aYPZN3eML9E4yqzKwBYVzIyfQxi3fqdlNP//kD9SC/LCdD05Btt0xx+/SYvIQ42YIDkB8jYIB64TAzU8oEQwSm1l2EvNfbxP8WElG+UDB8hirDuwcj7ILG8s0Fl+N3ArWM41VTXXGwyiyWKYHY5U6KEE4Vj6ZEhqomJG3XSplk+CQIIKhEKIKTpqK89f37w+m4zl3q+/qdxPwDitOPC/X7w/eMio8vJMbceMRH4wuNUfK7Hv71moINhurPrWudusiGWsv/aigtMdTnJ9U1b+Va1V3CZfkqav4vUEsHCH2i+qWNBAAAzhIAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAADwAAAHhsL3dvcmtib29rLnhtbI1UyW7bMBC99ytUwldbix3XMSwHrhwjAdIFcZocA0ocRWwoUiApLy367x1Rlus0RdCDTQ5n5s2bTbOLXSm8DWjDlYxJOAiIBzJTjMunmHy7W/UnxDOWSkaFkhCTPRhyMX832yr9nCr17KG/NDEprK2mvm+yAkpqBqoCiZpc6ZJaFPWTbyoNlJkCwJbCj4Jg7JeUS9IiTPX/YKg85xksVVaXIG0LokFQi+xNwStD5rOcC7hvE/JoVX2mJdJOqMiIPz/S/qq9lGbPdbVC65jkVBjARAu1/ZJ+h8xiRlQI4jFqITwPRp3JCwhl0RLD4GPzcM9ha/7oG9EhXinNfyhpqVhnWgkRE6vrQzQkann2L826KdQdTU33uHvgkqltTLBF+5P71l0fOLMFNnA8nIy6tyvgT4WNySQ8j4hnaXrbFComZwG65Vwb64I4FIqZbADjNRIm5J9k5HrWnZ50BXWeYUMVz2uGkd2cWFRtuOGpQMZ6ylGhr1nUIL7yjk68oze8h397J+uFx8BSLswJxPANiJFLqcuDQc4lsGYyXkpeXkvX0eNEFJwxkF0LhGpa1QXEqrV8HndCloNHHCULekmx0LTx3aQ4IhmwWh8nbN6W7X1v0RtNe1e90cw/CT9/ISE1jJbhpHKExcQSVUtsVth0T0P+STHEXWDdD/oj6YO8BGEp0hwEQRA2BYCdvTHWnYelEwrvrxZP8FRDu2pu64hXax6Tnx/G0TiZjKN+tAiH/TC8POt/HI7O+qvL1QpnLFkm56tfuIEOdYq/pOVvrMbPyS3k6z1uwa7dxoWj5KNV+++Y+d3yzH8DUEsHCH9JaLhmAgAAmQQAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAADQAAAHhsL3N0eWxlcy54bWztnd2TojgQwN/vr6CousdbQBRlS91ymPX2Htyaup2tuqmbe0CJSi0QK+Cu7l9/CZEvSTzxazJzcR+Abjrd+dFpAo7Z/odNGCjfAYp9GA1U452uKiCaQc+PFgP16+P4t56qxIkbeW4AIzBQtyBWPwx/6cfJNgBflgAkCm4higfqMklW7zUtni1B6Mbv4ApEWDOHKHQTfIgWWrxCwPViYhQGWkvXLS10/Ugd9qN1OA6TWJnBdZQM1HYuUujmDw/HZrVVhTbnQA+H8juIAHIDVWOe3KmePJlo9/fa09Oz8unT+8nkWRlNtIcJx9aq2mJDbP70lFtzzLpVM/1Z+ZWcqe16N+zPYVR00tBVKhn245/KdzdIRaRlNwT0eIR82r+5G/rBlgpbRDBbuijG7KlZ6oU2dUKDumDmU6pI0BoQ3cXhHGx/BgOIFLSYDtTx2Eg/R7g1znV7gfaF6EfF7cdx2zDvBere4exIN2SU+kGQj1JbpYJhf+UmCUDRGB8ou/3H7QqP8wgXRtpMet5/nL1A7tZodY43iGHgeySKhVPuNP60HIc0M60p8Cdtv9Tmmd7u0n8Mb6Px6G50d2Fv5KPrnL5RxUW9OTrHm21f3FsBbMpDfElv9r3R+3grb+TiMEkWlYDpLd3gkTeFyMNTkfwO2VUzmeL57gJGbvB1NVDnbhADNRfdwx9RJhz2AzBPsB/kL5Zkm8AVCQcmCQzxTmZDIqEtn+ZBSec/uAou8fwlc7cvxL73RTSQfemlowqB569DtYqhrrwalT3/B7jcIBjmJSr8CkHm1uGI4v+l+V+vQBwkfPMeZsql66MbXtCruzuYv1fzfl7xF6yq1OUvWmReKJyG/nc7eMoyA0HwhZj8Nc/nLS3yZL+Z199dROmBjnfxfGe3S1vaHbirVbAdQ9JI+nxFBXfpKRXRKPAXUQj2TnxAMAGzJH2Vk4qHfTc7UVlC5P/ETZPHnsXu1Ql585P4MyKi3VeVBGySP2Hi0lZwTD+Qu3rEwhyxH3mpY6yLl8iPvj3CsZ+rMaZVHoYSwNk34GVBLn0Pm5bO1DbzPVJ6wck4ldMuzn1QZXGZVJYkryeYlgyGE8zJY0sGI4ORwchgZDCnBNM2RbpTtg2homkLFU1LpGjsFw5GK0/f6WS+NI/vnDyP38zroZcDOjP21zapvxE2zpPQYWozLABIdGjtAlqrDM1gQzv34bEpskwiErKORNYUmSWRnVHNREQmTC3rSGRnZFlXIjtjYJpymtF8aB4D7fjZ+Ftjxh6bktmRzHqcZ4CWGPVMxIkGD5kpkTVF1pbImiLrSGRNkVkS2RG3zGNeAcnpLD/LBEEmYpZd4EXjBZkx3s6+UWiXnM6+Emq8gibIfFbEgsZD1pXImiITZKYhIrKr3QPe9Fd0vETryURrisyWyOo/rZV3gAuXM4mscZYJ8uAkDDJhH5xeBzNemhkyz5ozk19rNi5nklnzeiaZnTA25SuNxu9oDfkdXfOxeSay/+XzuaxnVWRd+cfa1xqe8jv0xswEeU0rIjK7QNaWadaYWXVoyr/VaF7OJLPmeSbrWfM8k8yOybOOvAc0zjNLMmvMrCuZNR6bPcmszEzb/Wa4tBJQ/vthSy1JFbI26UD9TBYIDkrUpms/SPyIHml1AweGoZudT+YpJQOTa6D8rf+TG1kVI4tptEYIRLNtbtOt2LQP2VR89Sp2XZbdA0DkeuUmdsWEroxawNyt9Yq35NJtgOfsDtFiWlljUtezxSf3NcWKqHUNz0bXs5VGWX54rfFsaHs8P2xNj9sfXe9xNUTHbo1n0+PaEDlbU6weyvLDtrHxh91T2zZNy+IQ3S1yu69xHB43y9J1Xmu82IgFzw/x1Iw1/2rzM+RwHvCu6aEM4fWUn4m8nvJZEw2bG109l6UplqSt29g27yrwcofqWBqSU2wb0yRXlZ2JjsMbwQ63umSrBO9rilVv6zlqWRw6+fLA9evDGyWmadtsTbEydt3GNNlRk9HI1/AiIDHwNGZ6n9L26reW1XWt+L8Ehv8CUEsHCJ/5skrvBQAAkGAAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAACwAAAF9yZWxzLy5yZWxzvdQ9T8MwEAbgvb8i8t64SaEglKQLQuqGUJFYjX35UGKfZV8h/HusClGKiukQdbR9995jDy7Wox6SN3C+Q1OyLF2wBIxE1ZmmZM/bh/ktW1ez4gkGQaHEt531SegxvmQtkb3j3MsWtPApWjDhpEanBYWla7gVshcN8HyxWHH3M4NVR5nJRpXMbVTGku2HhXOysa47CfcodxoMnRjxqyIkC9cAlWwc+Du6/hWxT0Mo46ct+fmWv+/JNZBQggSX6GBuXeh21IE/cBTKx7Dt9xUx0HLKx4GRwChQcZKwNia6mlIkd55Q//NE+5oY6Xp60kuY9k1J08Mu7wh0FtOsLq3JY5qbS2uWX5pZwY9+kOoTUEsHCJ9wMI4OAQAAeAQAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAEwAAAGN1c3RvbVhtbC9pdGVtMS54bWyNz09Lw0AQh+G7nyLsvTupBZWQpIeCpxYEFbwOm0my0J1ZdkbTj68R/x29zeV9fky7v6Rz9UZFo3Dntr52FXGQIfLUueen+82d2/dXbW5ykUzFImn1kbA2uXOzWW4ANMyUUH2KoYjKaD5IAhnHGAiu6/oGEhkOaAi/ivtiLhp/oGVZ/LLzUqY128LL6fj4aW8iqyEH+q5y+N965FEy2rx6t/CAxZjKQdiKnNX17SDhNRHbCRknWi/oW/j7bf8OUEsHCKHFlTW+AAAAIwEAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAGAAAAGN1c3RvbVhtbC9pdGVtUHJvcHMxLnhtbKWTwW4bIRCG730KizsL2AvsRrGjdtNIkRqpilopV8wONsoCK8B1q6rvXnadSyK1cZTjDPDP/80Ml1c/3bD4ATHZ4NeIVRQtwOvQW79bo+/fbnCDrjYfLvt00ausUg4RbjO4RXnmSy6t0T7n8YKQpPfgVKrCCL4cmhCdyiWMOxKMsRqugz448JksKRVEH4qWe3ADWhRtWyRvr9fod1dTyThtsOiaFa5FWwys5A3u5PKj7BrxaflZ/EGbyc+p4D2Y9Dyc9A7RnmNsVPpR7eDkyEFWEyPRhRGPsdyP2UJC5Dx9Z3UMKZhc6eCemE/K/RP5nfKl2tyD/Gv8jzLjrBUGKK4paFy3PcVKbjnecgZb4EIsgb5mazzEYYbsNckQXSLvArHehFHl/UQkyVcVs4fYBZ9jGP4N0pieS6MbzBsucM0Zw4o3gNuSbiXf8lbJt4D02tmpde9jeT7u8yd9PB6r42r28nD3hbC2bYhXDlLZI3gLBgzzFiTCKjajkBf7TF7+t81fUEsHCChVD3WCAQAAqQMAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAEwAAAGN1c3RvbVhtbC9pdGVtMi54bWxtzrEOgjAQxvHdpyDdoRoXY0pZkMk44QNgOaQJ7ZHeifL2FqODCfPv8v9OFS83JBMEsuhzscu2IgFvsLX+notrXaUHUeiNKhxhatAzeK7nEZJCqwqDq8GNQ8NASex4ykXPPB6lJNODayhz1gQk7Dgz6CT1TYARrWc57eU3xzEnu9gioVVpKfZmXaJ5uIhnewtNmJcpJX+oTq3l9ZOPqAs813kBJf8e129QSwcIFJI5+agAAAADAQAAUEsDBBQACAgIADc35lIAAAAAAAAAAAAAAAAYAAAAY3VzdG9tWG1sL2l0ZW1Qcm9wczMueG1stZTJbtswEIbveQpDd4qia2oJYgdOHAEBGiBIUiBXihraREVSIEd1i6LvXkrxpVnqFG2PHHK++Wfj2flX082+gA/a2WXC0iyZgZWu1Xa7TD491KRMzlcnZ204bQWKgM7DNYKZRTcbbWGZ7BD7U0qD3IERIXU92HipnDcC49FvqVNKS9g4ORiwSOdZllM5RJZ5NF0yi2wdkdebZfI921wwdllfkXXOL8jiaj0nZV3MCWdFVdcVK/mm/JGsRj1PAe9AhV+PI2/w+oUwo6V3wSlMpTMHTU9aDKAYs6PSWYwKH771kNB/Ru19rIlHDWGyrRG9bgaEcCzGfr9P9x+mEkYio483H++nt/9F3JtQxlmVK8jIIgNJFlWbEVE0nDScQQM8z+eQvelcqpYXSpaElzwnC84YEbwEUkVzVfCGV6L4+3Taw2zdCCu2ME0ZxiYerfBvydoq1wvcjSEKeis8WvCXcUS8695NfmUdeiE/R5UvZs8DeUc3Dvx+8N1EayWFbko5UJYy+ieOCN6Eox6vF0nHVfFWdNQ17Uigz1aSPv8yVj8BUEsHCLUZ22ChAQAAbAQAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAHgAAAGN1c3RvbVhtbC9fcmVscy9pdGVtMy54bWwucmVsc42PzQrCMBCE7z5F2LtNqyAiTXsRoTeRCl5Dum2DzQ/JVvTtDZ4sePC4szPfMGX9NBN7YIjaWQFFlgNDq1yn7SDg2p7We6irVXnBSVKyxFH7yFLGRgEjkT9wHtWIRsbMebTp07tgJKUzDNxLdZcD8k2e73j4ZkC1YLKmExCargDWvjz+w3Z9rxUenZoNWvpRwdUcyZmbmc7BpUbWyjAgCdCE5iNts8QEnvbxxcDqDVBLBwjvRNDnrgAAABcBAABQSwMEFAAICAgANzfmUgAAAAAAAAAAAAAAAB4AAABjdXN0b21YbWwvX3JlbHMvaXRlbTEueG1sLnJlbHONj80KwjAQhO8+Rdi7TetBREx7EaE3kQpeQ7pNg80PyVb07Q2eLHjwuLMz3zCH5mkn9sCYjHcCqqIEhk753jgt4Nqd1jto6tXhgpOkbEmjCYnljEsCRqKw5zypEa1MhQ/o8mfw0UrKZ9Q8SHWXGvmmLLc8fjOgXjBZ2wuIbV8B614B/2H7YTAKj17NFh39qOBqTuTtzU7n6HMj62TUSAIMof1IVZGZwPM+vhhYvwFQSwcIsmK2d64AAAAXAQAAUEsDBBQACAgIADc35lIAAAAAAAAAAAAAAAAeAAAAY3VzdG9tWG1sL19yZWxzL2l0ZW0yLnhtbC5yZWxzjY/NCsIwEITvPkXYu922BxFp2osIvYlU8BrSbRtsfkhS0bc3eLLgwePOznzDVM1Tz+xBPihrOBRZDoyMtL0yI4drd9ruoak31YVmEZMlTMoFljImcJhidAfEICfSImTWkUmfwXotYjr9iE7IuxgJyzzfof9mQL1isrbn4Nu+ANa9HP3DtsOgJB2tXDSZ+KMC5RKi1Tc9n71NjawTfqTIQUXSH6nMEhMw7cPVwPoNUEsHCOHUW0KuAAAAFwEAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAGAAAAGN1c3RvbVhtbC9pdGVtUHJvcHMyLnhtbGWQwWrDMAyG73uK4Lsjpy5JU5IU1lDotWywa3DkxlBbwXbKxti7z6GndcdfQt//oebwaW/ZHX0w5FpW5IJl6BSNxl1b9v524jt26F6aMezHIQ4hksdzRJulM5dmoWVTjPMeIKgJ7RBymtGlpSZvh5iivwJpbRT2pBaLLsJGiBLUklj2w95YltgmIc99y75rKcVOHLdciqLn26Koeb2pXnklKyH6shQnefxh3erzKLygDn/jylu8+SdmjfIUSMdckYUwDR5nMsnnLkGRi0ktfs0Iq3lg0DXwVALPT+h+AVBLBwjOe5vj3gAAAD4BAABQSwMEFAAICAgANzfmUgAAAAAAAAAAAAAAABMAAABjdXN0b21YbWwvaXRlbTMueG1s7VpZk5vGFn6/v4IizxJaQFtFSc1I9o0rHtuVUZa3VAsOI66BxnQzI/37nGbfxKZJaqpy7QcbON/pPt/ZetH3P54dW3oGn1nU3crT8USWwNWpYblPW/nXw/vRSv7xh/98r/ONTl0OLj9cPHjUT+AQCaEu2+h8K5849zaKwsL3bOxYuk8ZNflYp45CTdPSQZlNJgvFAU4MwomS0ybHihwyRJHnUw98bgEL391x7lvHgAOTJZz0n1tZlhyS/psb9hNxYCvvqR44+Kb89cN+K0/Okyn+ndyvJ3eaut6v96vleqGu1tr9++VutpvvVnfa8l4rY39L6ZyXP+2B6b7l8fDzzgfCQSKSCy+SEU9kXIY86mhgPP3YUWJyM21BVqsj6MZqOTXnZGaqK3OhzdZr1ZhNzbWMXjszY8PyvsIXKccvLy/jl/mY+k+C0anyx8PHyK9yKtxd1rvNdYkal82RNW26XpgwGakT0Efq2piMyPKojY7aFI6gLRYzmGQAdSuvTENbmvpqpK20xUjVptMR0VYwWuPr9VI7amuylCVO/CfgwuvMIzrcOmH0hk8pxj73AwgfTQtsgwnfHPXFUTXBBNU0NVirKwyalbpcGnNYzhbLtSyhoVFMogHhf2JvWY5HfS652Sw7saFchXfiJoGDDSIEQ/xWzlkbf0diPBvOIiqT6IJvAdYLqFWQhPQDcclT+OG6ImLbJR0+mFtZ8PQAhkUewX9GhzzErkDCLfezrgc+BumkYkAt9j1h/Bb8XcDpgTyxIdjPu1+GwPZYHw7kK7hDwB+pTkSh6YZVN48n4oPxu8VPvzKsM/1heyTXsnsBsc38ZLn8J8JOQ0z8L7jgh0YeLAeGaHj3DNHwO2oMUiCi4me4fKFoxqDQaAIrWWoo1bxRcrqTx1xCdkWE9e8f7RaGw3qU32oZUTiakzYNT++mzHJN6hF+ElqXyhficwyeHTZan4qgrfSHbn0oZvM99Z09mCSwsSV8C4htYTswrpf1262/XvNvJ6O+IVwpxGRjuQacsddgO7NsmxxtyLVFw2KeTS6fmlWcLMMANwfDjADfJXYLDpdQxmfXvsTIJIwtEfW59uIDw3WhLiqFdCRMNCeHbT5RDlmeFUClTGkmo9RZUkLW/QipqOlBSgX7JojJtcyUlOmknZUc5prBBdU3GXuAM38NY6Men9k5bbfz3Zn7ROdgSOEkmi1O9N/u2VjCIeeP4D7xk/RM7ADEnkLLiMjhb+cmv5DJGJr1y4+ikh7JUQS+iWDJLc4yPubtfJRw1ywuiL0JgytLtczsZb8wqNHUIxZq0G+Cn/JCNKOnZ1utKurBThX8JsgpL7Izcnq22KqiHuRUwW+iydYSM+vQZcvAa3a/ts1/R/v5/1amaSvT7YTsX7SVqZ51ZCVFbc+cCC4JfE3u1Cmvz5rqOVj8Zhed/yZzP+MDK2aSUP2ALrLSDGw6ihPSH5Cn0hkDzuycPAXukQZIgTHwtG+fEYQReBFn1iGGi0OeloORSMOdruMM+AcjwSd2ijfFeZcc1KgvumPoMKOhByglROqtkorMp+2KmyI2O2bLYlbrFbNSUUV97OZl3mbNryepeKSYUbToRhHCJYGXUgUtK4S6Mf+hRVNz06tUT+qBi19MrO+Es7CpYZX9irW4cjnnw6h6O9OziVrtwiPLZZxg7qTtNGuAXuDbIcTQldhkpkzHUyWTRTfk2m8eEH5JJSl2npZekvhUoUej/a5oOJPNPVYiyfVlQSBwcyJHm+pf00/fEdtubc0NVEYWJdvFFGEER9tyxexDXGy3gjYz5RtqQSvnykRVJjPUOcbB23t4nXteY/RQV2EKxZog1HzJeSBqBLvDn6UPSrXtxRqqss2XVoa+0cXVLvUb2u20/nYgtidSAJW+10XBxsJyxUW09Bs/oSt3Md+ArmuoabGdVGtr3P2kUPG1mXOL2y3DZoOoNeeGIf6adhYc/wc6H8SpkV3cDyD1K1xeqG+Ur3jaGL02GZu4TwEWmyHuxah6ov7lxplEyuKfI7yKLh+erQHKkkx0XcpJvHAQL5K9RvxOuvLncLJYtBSRMKoswQ6T+AkzP3CO4EvUlBh5xnfUl5IZsjHCQCKeZ1tR4ZJQCfZyD79ZuLyQsC9IgYcNQCwmeDYEMTG5JSD6KVU2rp1a1NlLNsS3gXlLWxdEtrgWoEbYOu5vdXqhQDmx1uEl5hHNCIakRIdb0dyiqHgQ8Eo77D4HBYN+eNIy/s3LiIiaol++YDpjAkafyru/4uavGBOevins/mr2ehVAbrPXQ7ymLcURUdqGVYOikCdd9q7FNGjbq3aTL+5FGxH3+90dY1S3xDrgHTZ0fhnmGlQUw7ts/mvoxKc0oFKtkco0qhMbmgSvyzxeGAfnQ7wZaJZNSMEKmAp2cHimKKK3PP9rTrmO6wypsa4rtGxs59C5JWAibO+MjmCJmb+ACT6EW7seYGPaT3zWT3zeT1ztJ679DZWpxhedQuCqM/qgjWlP+VlP+XlPebWnvNZR/iCWNMOSRUA7n6y+RkSkAw6ebveUDseq69CvYUSPaE7mUSespEs88VD3Q/Uf/gJQSwcInRNfJ4gHAADmLgAAUEsDBBQACAgIADc35lIAAAAAAAAAAAAAAAATAAAAZG9jUHJvcHMvY3VzdG9tLnhtbLWUW2+CMBSA3/crSN+RiiLUAAaKZkv2sMTLewdVyKBt2uo0y/77yhDNnpZs2qfTS77vnNO04ezY1NaBSlVxFoHhAAKLspwXFdtFYL1a2AGwlCasIDVnNAInqsAsfghfJBdU6ooqyxCYikCptZg6jspL2hA1MNvM7Gy5bIg2U7lz+HZb5TTj+b6hTDsuhBMn3yvNG1tccKDjTQ/6r8iC5212arM6CcOLwzP8ZG0bXRUR+Mg8nGUe9Gx3jrA9hMPURiPk2zCA0E1dvEDJ/BNYoj3sAouRxlSeCLHp+mSQBz2txbvSMh66A2hG6FyXQqc3/tM96t2YM23qawt6Kn7o4REaBoQpgok3RhnKAh9NxgHy0oWPXTzCQeL5qXeX/MZ9fuYCljTfy0qfuuyqcdy1xAQ303m97tH0QdYVe1O4JGxHzy155bw+a7/Dm4knvfi5da7FimdE0ztL/V66zElNsUHdWRhchCWR7Zv63edcv4H4C1BLBwi1jsMXcQEAAEsEAABQSwMEFAAICAgANzfmUgAAAAAAAAAAAAAAABEAAABkb2NQcm9wcy9jb3JlLnhtbH1SXW+CMBR9368gfYcWVDQEMNkWn2ayRJYte+vKFbtBadoq+u9XQHBuZm/33HN67lfj5bEqnQMozWuRIN8jyAHB6pyLIkEv2cpdIEcbKnJa1gISdAKNluldzGTEagXPqpagDAftWCOhIyYTtDNGRhhrtoOKas8qhCW3taqosVAVWFL2RQvAASEhrsDQnBqKW0NXjo7obJmz0VLuVdkZ5AxDCRUIo7Hv+fiiNaAqffNBx/xQVtycJNyUDuSoPmo+Cpum8ZpJJ7X9+/ht/bTpRnW5aFfFAKXxuZGIKaAGcscaRH25gXmdPDxmK5QGJCAumbokyPxpNFtE0/A9xr/et4Z9XKu0ZS/Axjloprg09oY9eZWwuKSi2NuFpyDcl00nGVPtKUuqzdoefcshvz9Zjxu5oaPqnPt/JN8lc5eEGVlEs/n1SINBV1nBgbd/L/XDruqI27b1/uMTmOlnGoGNDTcl9Okh/PMh029QSwcISRYRPWkBAADcAgAAUEsDBBQACAgIADc35lIAAAAAAAAAAAAAAAAQAAAAZG9jUHJvcHMvYXBwLnhtbJ2QzWrDMBCE730KI3K1ZTutCUFWaCk9BdqDW3ozirROVPSHtAnO21dtIcm5tx1m+XZn2Ga2pjhBTNq7njRVTQpw0ivt9j15H17KFSkSCqeE8Q56coZENvyOvUUfIKKGVGSCSz05IIY1pUkewIpUZdtlZ/LRCswy7qmfJi3h2cujBYe0reuOwozgFKgyXIDkj7g+4X+hysuf/9LHcA6Zx9kANhiBwBm9joNHYQZtgbftQzYukj2GYLQUmDvhW72L8Pp7hHZVU3XVcrHV7jiPn6tu7O6Lm4Uxh/gCibSpF09HbVS5ZPQWxui1N/4NUEsHCBWgAinsAAAAfAEAAFBLAwQUAAgICAA3N+ZSAAAAAAAAAAAAAAAAEwAAAFtDb250ZW50X1R5cGVzXS54bWzFVltPgzAUft+vILya0V0SY8zYHrw86hJn4pup9DDq6CVtd/v3tuyimww2IfhUoP1up6cNg9GKpd4ClKaCh3436Pge8EgQyqeh/zp5bN/4o2FrMFlL0J5dy3XoJ8bIW4R0lADDOhASuJ2JhWLY2Fc1RRJHMzwF1Ot0rlEkuAFu2sZx+MPBPcR4nhrvYWU/b3Qt3PfuNuucVOhjKVMaYWOnkZtFuTgFqS4ALjg5ctfeOgssMlujEyr11WkFyadHApS5ZO57PuJTQj4km7CYZ1tuRQl4Y6zME2Z2AVqliCi8tFXXu4duUFyVnHAijmkERERzZiHBlmiX7oTuuysiWgo1+xBi5kSDmst6QlgnWAF5McqlrhxWSwWY6ATA2AQH3CU+XPQMp7e1yF66DVbih4Ns6NdcjT3/hT6qt2A9Pnr/6GN3KGptTjsGDFNedkLMOoXaj0ZGWqS8OQVN9H4010awN5YiaoCVtdvZRGMlpK7evHtSxwfK0OKyHXoo69nLwlS/Ef4cZtMNzkm/qSsxT7yx+zhPvNe8+H7rq19+lfq4rPMKiKyBLMGWsaYcbXlWim9xoeBy6d3OOfSlipa6clZwP5QEyG/t1gBlf+PDL1BLBwgTKVCizQEAALwLAABQSwECFAAUAAgICAA3N+ZSx81Oq6gAAAArAQAAGAAAAAAAAAAAAAAAAAAAAAAAeGwvZHJhd2luZ3MvZHJhd2luZzEueG1sUEsBAhQAFAAICAgANzfmUnIpWCzhAAAAPwMAABoAAAAAAAAAAAAAAAAA7gAAAHhsL19yZWxzL3dvcmtib29rLnhtbC5yZWxzUEsBAhQAFAAICAgANzfmUp5kMREzAwAATxUAABQAAAAAAAAAAAAAAAAAFwIAAHhsL3NoYXJlZFN0cmluZ3MueG1sUEsBAhQAFAAICAgANzfmUsLVMZeoAAAAGgEAACMAAAAAAAAAAAAAAAAAjAUAAHhsL3dvcmtzaGVldHMvX3JlbHMvc2hlZXQxLnhtbC5yZWxzUEsBAhQAFAAICAgANzfmUtv+TOLrBwAATisAABgAAAAAAAAAAAAAAAAAhQYAAHhsL3dvcmtzaGVldHMvc2hlZXQzLnhtbFBLAQIUABQACAgIADc35lLH72orxwgAAPEuAAAYAAAAAAAAAAAAAAAAALYOAAB4bC93b3Jrc2hlZXRzL3NoZWV0MS54bWxQSwECFAAUAAgICAA3N+ZSfaL6pY0EAADOEgAAGAAAAAAAAAAAAAAAAADDFwAAeGwvd29ya3NoZWV0cy9zaGVldDIueG1sUEsBAhQAFAAICAgANzfmUn9JaLhmAgAAmQQAAA8AAAAAAAAAAAAAAAAAlhwAAHhsL3dvcmtib29rLnhtbFBLAQIUABQACAgIADc35lKf+bJK7wUAAJBgAAANAAAAAAAAAAAAAAAAADkfAAB4bC9zdHlsZXMueG1sUEsBAhQAFAAICAgANzfmUp9wMI4OAQAAeAQAAAsAAAAAAAAAAAAAAAAAYyUAAF9yZWxzLy5yZWxzUEsBAhQAFAAICAgANzfmUqHFlTW+AAAAIwEAABMAAAAAAAAAAAAAAAAAqiYAAGN1c3RvbVhtbC9pdGVtMS54bWxQSwECFAAUAAgICAA3N+ZSKFUPdYIBAACpAwAAGAAAAAAAAAAAAAAAAACpJwAAY3VzdG9tWG1sL2l0ZW1Qcm9wczEueG1sUEsBAhQAFAAICAgANzfmUhSSOfmoAAAAAwEAABMAAAAAAAAAAAAAAAAAcSkAAGN1c3RvbVhtbC9pdGVtMi54bWxQSwECFAAUAAgICAA3N+ZStRnbYKEBAABsBAAAGAAAAAAAAAAAAAAAAABaKgAAY3VzdG9tWG1sL2l0ZW1Qcm9wczMueG1sUEsBAhQAFAAICAgANzfmUu9E0OeuAAAAFwEAAB4AAAAAAAAAAAAAAAAAQSwAAGN1c3RvbVhtbC9fcmVscy9pdGVtMy54bWwucmVsc1BLAQIUABQACAgIADc35lKyYrZ3rgAAABcBAAAeAAAAAAAAAAAAAAAAADstAABjdXN0b21YbWwvX3JlbHMvaXRlbTEueG1sLnJlbHNQSwECFAAUAAgICAA3N+ZS4dRbQq4AAAAXAQAAHgAAAAAAAAAAAAAAAAA1LgAAY3VzdG9tWG1sL19yZWxzL2l0ZW0yLnhtbC5yZWxzUEsBAhQAFAAICAgANzfmUs57m+PeAAAAPgEAABgAAAAAAAAAAAAAAAAALy8AAGN1c3RvbVhtbC9pdGVtUHJvcHMyLnhtbFBLAQIUABQACAgIADc35lKdE18niAcAAOYuAAATAAAAAAAAAAAAAAAAAFMwAABjdXN0b21YbWwvaXRlbTMueG1sUEsBAhQAFAAICAgANzfmUrWOwxdxAQAASwQAABMAAAAAAAAAAAAAAAAAHDgAAGRvY1Byb3BzL2N1c3RvbS54bWxQSwECFAAUAAgICAA3N+ZSSRYRPWkBAADcAgAAEQAAAAAAAAAAAAAAAADOOQAAZG9jUHJvcHMvY29yZS54bWxQSwECFAAUAAgICAA3N+ZSFaACKewAAAB8AQAAEAAAAAAAAAAAAAAAAAB2OwAAZG9jUHJvcHMvYXBwLnhtbFBLAQIUABQACAgIADc35lITKVCizQEAALwLAAATAAAAAAAAAAAAAAAAAKA8AABbQ29udGVudF9UeXBlc10ueG1sUEsFBgAAAAAXABcAHAYAAK4+AAAAAA==\"\n" +
            "}";

    public static EventMessageDto initEventMessageDto() {
        EventMessageDto eventMessageDto = new EventMessageDto();

        eventMessageDto.getHeader().setTimestamp(Instant.parse("2021-05-31T05:13:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayFrom(Instant.parse("2021-05-31T00:00:00Z"));
        eventMessageDto.getHeader().getProperties().getBusinessDataIdentifier().setBusinessDayTo(Instant.parse("2021-05-31T23:59:59Z"));

        TimeserieTemporalDataDto timeserieTemporalDataDto_Event = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Event.setLabel(EVENT_KEY);
        timeserieTemporalDataDto_Event.setValue("Event A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_Constraint = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_Constraint.setLabel(CONSTRAINT_KEY);
        timeserieTemporalDataDto_Constraint.setValue("Constraint A");
        TimeserieTemporalDataDto timeserieTemporalDataDto_RemedialActions = new TimeserieTemporalDataDto();
        timeserieTemporalDataDto_RemedialActions.setLabel(REMEDIAL_ACTIONS_KEY);
        timeserieTemporalDataDto_RemedialActions.setValue("RemedialActions A");
        TimeserieDataDetailsDto timeserieDataDetailsDto = new TimeserieDataDetailsDto();
        timeserieDataDetailsDto.setTimestamp(OffsetDateTime.of(2021, 5, 31, 0, 0, 0, 0, ZoneOffset.UTC));
        timeserieDataDetailsDto.setDetail(Arrays.asList(
                timeserieTemporalDataDto_Event,
                timeserieTemporalDataDto_Constraint,
                timeserieTemporalDataDto_RemedialActions
        ));
        TimeserieDataDto timeserieDataDto = new TimeserieDataDto();
        timeserieDataDto.setData(Collections.singletonList(timeserieDataDetailsDto));
        eventMessageDto.getPayload().setTimeserie(Collections.singletonList(timeserieDataDto));

        return eventMessageDto;
    }

    public static EventMessage initEventMessage(FileTypeEnum fileTypeEnum) {
        return initEventMessage(null, fileTypeEnum);
    }

    public static EventMessage initEventMessage(Long id, FileTypeEnum fileTypeEnum) {
        return EventMessage.builder()
                .id(id)
                .source("Service A")
                .messageTypeName("Coordination A")
                .timestamp(Instant.parse("2021-05-31T05:13:00Z"))
                .businessApplication("PanEuropeanServiceATool")
                .businessDayFrom(Instant.parse("2021-05-31T00:00:00Z"))
                .businessDayTo(Instant.parse("2021-05-31T23:59:59Z"))
                .sendingUser("22XCORESO------S")
                .eventMessageRecipients(Arrays.asList(
                        EventMessageRecipient.builder()
                                .eicCode("10XFR-RTE------Q")
                                .build(),
                        EventMessageRecipient.builder()
                                .eicCode("10X1001A1001A345")
                                .build()
                ))
                .timeseries(Arrays.asList(
                        Timeserie.builder()
                                .id(2L)
                                .name("Coordination")
                                .timeserieDatas(Arrays.asList(
                                        TimeserieData.builder()
                                                .id(3L)
                                                .timestamp(OffsetDateTime.of(2021, 7, 9, 1, 30, 0, 0, ZoneOffset.UTC))
                                                .timeserieDataDetailses(Arrays.asList(
                                                        TimeserieDataDetails.builder().label(EVENT_KEY).value("Event A").build(),
                                                        TimeserieDataDetails.builder().label(CONSTRAINT_KEY).value("Constraint A").build(),
                                                        TimeserieDataDetails.builder().label(REMEDIAL_ACTIONS_KEY).value("RemedialActions A")
                                                                .timeserieDataDetailsResults(new ArrayList<>()).build()
                                                ))
                                                .build(),
                                        TimeserieData.builder()
                                                .id(4L)
                                                .timestamp(OffsetDateTime.of(2021, 7, 10, 1, 30, 0, 0, ZoneOffset.UTC))
                                                .timeserieDataDetailses(Arrays.asList(
                                                        TimeserieDataDetails.builder().label(EVENT_KEY).value("Event B").build(),
                                                        TimeserieDataDetails.builder().label(CONSTRAINT_KEY).value("Constraint B").build(),
                                                        TimeserieDataDetails.builder().label(REMEDIAL_ACTIONS_KEY).value("RemedialActions B")
                                                                .timeserieDataDetailsResults(new ArrayList<>()).build()
                                                ))
                                                .build()))
                                .build()
                ))
                .eventMessageFiles(Stream.of(fileTypeEnum == FileTypeEnum.EXCEL ? initEventMessageExcelFile(FileDirectionEnum.INPUT)
                        : initEventMessageJsonFile(FileDirectionEnum.INPUT)).collect(Collectors.toList()))
                .build();
    }

    public static Coordination initCoordination(FileTypeEnum fileTypeEnum) {
        Coordination coordination = new Coordination();
        coordination.setId(1L);
        coordination.setEventMessage(initEventMessage(fileTypeEnum));
        coordination.setProcessKey("coordinationProcessKey");
        coordination.setPublishDate(Instant.parse("2021-05-31T05:13:00Z"));
        coordination.setStartDate(Instant.parse("2021-05-31T00:00:00Z"));
        coordination.setEndDate(Instant.parse("2021-05-31T23:59:59Z"));
        coordination.setStatus(null);
        coordination.setCoordinationGeneralComments(Arrays.asList(
                CoordinationGeneralComment.builder()
                        .eicCode("10XFR-RTE------Q")
                        .generalComment("Not ok!")
                        .build()
        ));
        coordination.setCoordinationRas(Arrays.asList(
                CoordinationRa.builder()
                        .id(2L)
                        .idTimeserieData(3L)
                        .event("Event A")
                        .constraintt("Constraint A")
                        .remedialAction("RemedialActions A")
                        .coordinationRaAnswers(Arrays.asList(
                                CoordinationRaAnswer.builder()
                                        .id(3L)
                                        .eicCode("10XFR-RTE------Q")
                                        .answer(CoordinationEntityRaResponseEnum.NOK)
                                        .explanation("Explanation 1")
                                        .comment("Not ok!")
                                        .build(),
                                CoordinationRaAnswer.builder()
                                        .id(4L)
                                        .eicCode("10X1001A1001A345")
                                        .answer(CoordinationEntityRaResponseEnum.OK)
                                        .build()
                        ))
                        .build(),
                CoordinationRa.builder()
                        .id(5L)
                        .idTimeserieData(4L)
                        .event("Event B")
                        .constraintt("Constraint B")
                        .remedialAction("RemedialActions B")
                        .coordinationRaAnswers(Arrays.asList(
                                CoordinationRaAnswer.builder()
                                        .id(6L)
                                        .eicCode("10XFR-RTE------Q")
                                        .answer(CoordinationEntityRaResponseEnum.NOK)
                                        .explanation("Explanation 1")
                                        .comment("Not ok!")
                                        .build(),
                                CoordinationRaAnswer.builder()
                                        .id(7L)
                                        .eicCode("10X1001A1001A345")
                                        .answer(CoordinationEntityRaResponseEnum.OK)
                                        .build()
                        ))
                        .build()
        ));
        return coordination;
    }

    public static EventMessageFile initEventMessageExcelFile(FileDirectionEnum fileDirectionEnum) {
        try {
            KafkaFileWrapperDto kafkaFileWrapperDto = new ObjectMapper().readValue(KAFKA_EXCEL_DATA, KafkaFileWrapperDto.class);
            return EventMessageFile.builder()
                    .id(1L)
                    .creationDate(Instant.now())
                    .fileName(kafkaFileWrapperDto.getFileName())
                    .fileType(kafkaFileWrapperDto.getFileType())
                    .fileContent(kafkaFileWrapperDto.getFileContent())
                    .fileDirection(fileDirectionEnum)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

    public static EventMessageFile initEventMessageJsonFile(FileDirectionEnum fileDirectionEnum) {
        try {
            KafkaFileWrapperDto kafkaFileWrapperDto = new ObjectMapper().readValue(KAFKA_JSON_DATA, KafkaFileWrapperDto.class);
            return EventMessageFile.builder()
                    .id(1L)
                    .creationDate(Instant.now())
                    .fileName(kafkaFileWrapperDto.getFileName())
                    .fileType(kafkaFileWrapperDto.getFileType())
                    .fileContent(kafkaFileWrapperDto.getFileContent())
                    .fileDirection(fileDirectionEnum)
                    .build();
        } catch (Exception e) {
            return null;
        }
    }

}
