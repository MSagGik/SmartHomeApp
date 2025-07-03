package io.github.msaggik.db.entity.config

object DatabaseConfig {
    // SMART HOME DB
    const val DATABASE_NAME: String = "msaggik.smarthome.db"

    // ROOM HOME
    const val ROOM_HOME_TABLE: String = "room_home_table"

    const val ROOM_HOME_ID: String = "room_home_id"
    const val ROOM_HOME_NAME: String = "room_home_name"
    const val ROOM_HOME_DESCRIPTION: String = "room_home_description"
    const val ROOM_HOME_IMAGE_URI: String = "room_home_image_uri"
    const val ROOM_HOME_DATE_CREATE: String = "room_home_date_create"

    // DEVICE
    const val DEVICE_TABLE: String = "device_table"

    const val DEVICE_ID: String = "device_id"
    const val DEVICE_MAC_ADDRESS: String = "device_mac_address"
    const val DEVICE_NAME: String = "device_name"
    const val DEVICE_TYPE: String = "device_type"
    const val DEVICE_LIST_TYPE_IOT: String = "device_list_type_iot"
    const val DEVICE_DATE_CREATE: String = "device_date_create"

    // ROOM HOME AND DEVICE
    const val ROOM_HOME_AND_DEVICE_TABLE: String = "room_home_and_device_table"

    const val ID_ROOM_HOME: String = "id_room_home"
    const val ID_MAC_DEVICE: String = "id_mac_device"
    const val ROOM_HOME_AND_DEVICE_DATE_CREATE: String = "date_create"

    // LIGHTING MODE HOME
    const val LIGHTING_MODE_TABLE: String = "lighting_mode_table"

    const val LIGHTING_MODE_ID: String = "lighting_mode_id"
    const val LIGHTING_MODE_NAME: String = "lighting_mode_name"
    const val LIGHTING_MODE_IMAGE_URI: String = "lighting_mode_image_uri"
    const val LIGHTING_MODE_COLORS: String = "lighting_mode_colors"
    const val LIGHTING_MODE_DATE_CREATE: String = "lighting_mode_date_create"
}