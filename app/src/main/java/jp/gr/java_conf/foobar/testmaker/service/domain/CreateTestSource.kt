package jp.gr.java_conf.foobar.testmaker.service.domain

enum class CreateTestSource(val title: String) {
    SELF("self"),
    DYNAMIC_LINKS("dynamic_links"),
    FILE_IMPORT("file"),
    SELF_DOWNLOAD("self-download"),
    PUBLIC_DOWNLOAD("public-download"),
    GROUP_DOWNLOAD("group-download"),
    UPDATED_BY_OTHERS("updated_by_others"),
}