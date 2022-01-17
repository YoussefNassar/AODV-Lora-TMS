package lora;

public enum LoraCommand {
    AT("AT"),
    EOF("\r\n"),
    UNKNOWN("UNKNOWN"),

    AT_RST("AT+RST"),
    AT_ADDR_SET("AT+ADDR="),
    AT_SEND("AT+SEND="),


    // Lora reply codes
    REPLY_OK("AT,OK"),
    REPLY_SENDING("AT,SENDING"),
    REPLY_SENDED("AT,SENDED"),

    AT_CFG("AT+CFG="),
    ;
    public final String CODE;

    LoraCommand(String CODE) {
        this.CODE = CODE;
    }

    public static LoraCommand valueOfCode(String code) {
        for (LoraCommand e : values()) {
            if (e.CODE.equals(code)) {
                return e;
            }
        }
        return LoraCommand.UNKNOWN;
    }
}
