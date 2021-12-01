package lora;

public enum LoraCommand {
    AT("AT"),
    EOF("\r\n"),

    AT_RST("AT+RST"),
    AT_ADDR_SET("AT+ADDR="),
    AT_SEND("AT+SEND="),

    AT_CFG("AT+CFG="),
    ;
    public final String CODE;

    LoraCommand(String CODE) {
        this.CODE = CODE;
    }
}
