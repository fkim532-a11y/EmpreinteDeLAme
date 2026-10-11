package com.francekim.empreintedelame.data.model

data class HumanDesignTypeInfo(
    val name: String,
    val keyword: String,
    val strategy: String,
    val notSelfTheme: String,
    val description: String
)

data class HumanDesignAuthorityInfo(
    val name: String,
    val keyword: String,
    val description: String
)

data class HumanDesignProfileInfo(
    val name: String,
    val description: String
)

data class HumanDesignDefinitionInfo(
    val name: String,
    val description: String
)

data class HumanDesignCenterInfo(
    val name: String,
    val definedDescription: String,
    val undefinedDescription: String
)

data class HumanDesignChannelInfo(
    val name: String,
    val keyword: String,
    val description: String
)

data class HumanDesignGateInfo(
    val name: String,
    val keyword: String,
    val description: String
)

data class HumanDesignContent(
    val types: Map<String, HumanDesignTypeInfo>,
    val authorities: Map<String, HumanDesignAuthorityInfo>,
    val profiles: Map<String, HumanDesignProfileInfo>,
    val definitions: Map<String, HumanDesignDefinitionInfo>,
    val centers: Map<String, HumanDesignCenterInfo>,
    val channels: Map<String, HumanDesignChannelInfo>,
    val gates: Map<Int, HumanDesignGateInfo>
)
