package jp.gr.java_conf.foobar.testmaker.service.domain

enum class CreateTestSource(val title: String) {
    SELF("self"),
    DYNAMIC_LINKS("dynamic_links"),
    FILE_IMPORT("file"),
    SELF_DOWNLOAD("self_download"),
    PUBLIC_DOWNLOAD("public_download"),
}