package com.nntk.nba0708.entity


data  class  TeamMeta(
    val id: Int?,
    val teamName: String?,
    val logoInfo: LogoInfo?
)

data class LogoInfo(
    val logoName: String?,
    val size: Int?,
    val loopIndex: Int?
)

