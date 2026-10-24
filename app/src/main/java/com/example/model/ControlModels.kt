package com.example.model

enum class ControlProfile(val titleVi: String, val subtitleVi: String) {
    ON_FOOT("Đi Bộ / Bắn Súng (Đầy Đủ)", "Full phím GTA 5: WASD, Shift, Ctrl, Alt, F, E, R, Q, G, X, C, V, 1-5 + Chuột Ảo"),
    DRIVING("Lái Xe / Đua Xe", "Vô lăng A/D + Chân Ga / Phanh + Drift + Còi E + Đèn H + Dây an toàn G"),
    XBOX_GAMEPAD("Tay Cầm Xbox Ảo", "Cần Analog kép + Cụm ABXY + D-Pad + Cò LB/LT/RB/RT"),
    TOUCH_ONLY("Tối Giản (Chỉ Chuột Ảo)", "Ẩn tay cầm, chỉ giữ Con trỏ Chuột PC để bấm menu web")
}

enum class MouseControlMode(val titleVi: String, val badgeVi: String) {
    VIRTUAL_CURSOR("Con Trỏ Chuột PC (Di chuyển mũi tên & Click)", "CHUỘT PC"),
    CAMERA_3D("Khóa Tâm 3D (Vuốt xoay góc nhìn 360°)", "TÂM 3D")
}

enum class MovementStyle(val titleVi: String) {
    ANALOG_STICK("Cần Analog 360°"),
    WASD_BUTTONS("4 Phím W / A / S / D Rời")
}

data class PcKeySpec(
    val id: String,
    val label: String,
    val shortText: String,
    val key: String,
    val code: String,
    val keyCode: Int,
    val isMouse: Boolean = false,
    val mouseButton: Int = 0, // 0 = Left, 2 = Right, 1 = Middle
    val wheelDelta: Int = 0,  // != 0 for scroll wheel
    val gamepadButtonIndex: Int = -1
)

object PcKeyCatalog {
    // Mouse & Wheel
    val MOUSE_LEFT = PcKeySpec("MOUSE_LEFT", "Chuột Trái (Bắn/Click)", "LMB", "MouseLeft", "MouseLeft", 1, isMouse = true, mouseButton = 0, gamepadButtonIndex = 7)
    val MOUSE_RIGHT = PcKeySpec("MOUSE_RIGHT", "Chuột Phải (Ngắm)", "RMB", "MouseRight", "MouseRight", 2, isMouse = true, mouseButton = 2, gamepadButtonIndex = 6)
    val MOUSE_MIDDLE = PcKeySpec("MOUSE_MIDDLE", "Chuột Giữa", "MMB", "MouseMiddle", "MouseMiddle", 4, isMouse = true, mouseButton = 1)
    val WHEEL_UP = PcKeySpec("WHEEL_UP", "Cuộn Lên (Đổi Súng +)", "LĂN ↑", "WheelUp", "WheelUp", 0, wheelDelta = -120)
    val WHEEL_DOWN = PcKeySpec("WHEEL_DOWN", "Cuộn Xuống (Đổi Súng -)", "LĂN ↓", "WheelDown", "WheelDown", 0, wheelDelta = 120)

    // Movement
    val KEY_W = PcKeySpec("KEY_W", "Tiến / Ga (W)", "W", "w", "KeyW", 87, gamepadButtonIndex = 12)
    val KEY_S = PcKeySpec("KEY_S", "Lùi / Phanh (S)", "S", "s", "KeyS", 83, gamepadButtonIndex = 13)
    val KEY_A = PcKeySpec("KEY_A", "Trái (A)", "A", "a", "KeyA", 65, gamepadButtonIndex = 14)
    val KEY_D = PcKeySpec("KEY_D", "Phải (D)", "D", "d", "KeyD", 68, gamepadButtonIndex = 15)

    // Modifiers & Core Actions
    val KEY_SPACE = PcKeySpec("KEY_SPACE", "Nhảy / Phanh Tay (Space)", "SPACE", " ", "Space", 32, gamepadButtonIndex = 0)
    val KEY_SHIFT = PcKeySpec("KEY_SHIFT", "Chạy Nhanh / Nitro (Shift)", "SHIFT", "Shift", "ShiftLeft", 16, gamepadButtonIndex = 10)
    val KEY_CTRL = PcKeySpec("KEY_CTRL", "Ngồi / Rón Rén (Ctrl)", "CTRL", "Control", "ControlLeft", 17, gamepadButtonIndex = 1)
    val KEY_ALT = PcKeySpec("KEY_ALT", "Mắt Thần / Nhân Vật (Alt)", "ALT", "Alt", "AltLeft", 18)

    // Primary GTA 5 / FiveM Action Keys
    val KEY_F = PcKeySpec("KEY_F", "Vào/Ra Xe (F)", "F", "f", "KeyF", 70, gamepadButtonIndex = 3)
    val KEY_E = PcKeySpec("KEY_E", "Tương Tác / Còi (E)", "E", "e", "KeyE", 69, gamepadButtonIndex = 1)
    val KEY_R = PcKeySpec("KEY_R", "Nạp Đạn / Đánh (R)", "R", "r", "KeyR", 82, gamepadButtonIndex = 2)
    val KEY_Q = PcKeySpec("KEY_Q", "Ẩn Nấp / Radio (Q)", "Q", "q", "KeyQ", 81, gamepadButtonIndex = 4)
    val KEY_G = PcKeySpec("KEY_G", "Lựu Đạn / Dây An Toàn (G)", "G", "g", "KeyG", 71, gamepadButtonIndex = 5)
    val KEY_C = PcKeySpec("KEY_C", "Ngồi / Nhìn Sau (C)", "C", "c", "KeyC", 67, gamepadButtonIndex = 11)
    val KEY_V = PcKeySpec("KEY_V", "Đổi Góc Nhìn (V)", "V", "v", "KeyV", 86, gamepadButtonIndex = 8)
    val KEY_X = PcKeySpec("KEY_X", "Giơ Tay / Huỷ Hành Động (X)", "X", "x", "KeyX", 88)
    val KEY_Z = PcKeySpec("KEY_Z", "Danh Sách / Tầm Voice (Z)", "Z", "z", "KeyZ", 90)
    val KEY_B = PcKeySpec("KEY_B", "Chỉ Tay / Khóa Xe (B)", "B", "b", "KeyB", 66)
    val KEY_N = PcKeySpec("KEY_N", "Bật Mic Voice (N)", "N", "n", "KeyN", 78)
    val KEY_H = PcKeySpec("KEY_H", "Đèn Pha Xe (H)", "H", "h", "KeyH", 72, gamepadButtonIndex = 16)

    // Menus, Chat, Inventory
    val KEY_TAB = PcKeySpec("KEY_TAB", "Vòng Vũ Khí (Tab)", "TAB", "Tab", "Tab", 9, gamepadButtonIndex = 4)
    val KEY_M = PcKeySpec("KEY_M", "Menu Tương Tác (M)", "M", "m", "KeyM", 77, gamepadButtonIndex = 8)
    val KEY_T = PcKeySpec("KEY_T", "Mở Khung Chat (T)", "T", "t", "KeyT", 84)
    val KEY_Y = PcKeySpec("KEY_Y", "Menu Quần Áo / Động Tác (Y)", "Y", "y", "KeyY", 89)
    val KEY_U = PcKeySpec("KEY_U", "Ragdoll / Cốp Xe (U)", "U", "u", "KeyU", 85)
    val KEY_I = PcKeySpec("KEY_I", "Túi Đồ / Inventory (I)", "I", "i", "KeyI", 73)
    val KEY_P = PcKeySpec("KEY_P", "Bản Đồ Lớn (P)", "P", "p", "KeyP", 80)
    val KEY_K = PcKeySpec("KEY_K", "Menu Phụ Kiện (K)", "K", "k", "KeyK", 75)
    val KEY_L = PcKeySpec("KEY_L", "Khóa Cửa Xe (L)", "L", "l", "KeyL", 76)

    // System & Navigation Keys
    val KEY_ESC = PcKeySpec("KEY_ESC", "Bản Đồ / Pause (Esc)", "ESC", "Escape", "Escape", 27, gamepadButtonIndex = 9)
    val KEY_ENTER = PcKeySpec("KEY_ENTER", "Chọn / Xác Nhận (Enter)", "ENTER", "Enter", "Enter", 13, gamepadButtonIndex = 0)
    val KEY_BACKSPACE = PcKeySpec("KEY_BACKSPACE", "Quay Lại / Xoá (Backspace)", "BACK", "Backspace", "Backspace", 8, gamepadButtonIndex = 1)

    // Arrow Keys (Phone & GTA Menus)
    val KEY_UP = PcKeySpec("KEY_UP", "Mũi Tên Lên / Đ.Thoại (↑)", "↑", "ArrowUp", "ArrowUp", 38, gamepadButtonIndex = 12)
    val KEY_DOWN = PcKeySpec("KEY_DOWN", "Mũi Tên Xuống (↓)", "↓", "ArrowDown", "ArrowDown", 40, gamepadButtonIndex = 13)
    val KEY_LEFT = PcKeySpec("KEY_LEFT", "Mũi Tên Trái (←)", "←", "ArrowLeft", "ArrowLeft", 37, gamepadButtonIndex = 14)
    val KEY_RIGHT = PcKeySpec("KEY_RIGHT", "Mũi Tên Phải (→)", "→", "ArrowRight", "ArrowRight", 39, gamepadButtonIndex = 15)

    // Number Hotkeys 1..6
    val KEY_1 = PcKeySpec("KEY_1", "Phím Số 1 (Súng 1)", "1", "1", "Digit1", 49)
    val KEY_2 = PcKeySpec("KEY_2", "Phím Số 2 (Súng 2)", "2", "2", "Digit2", 50)
    val KEY_3 = PcKeySpec("KEY_3", "Phím Số 3 (Súng 3)", "3", "3", "Digit3", 51)
    val KEY_4 = PcKeySpec("KEY_4", "Phím Số 4 (Súng 4)", "4", "4", "Digit4", 52)
    val KEY_5 = PcKeySpec("KEY_5", "Phím Số 5 (Súng 5)", "5", "5", "Digit5", 53)
    val KEY_6 = PcKeySpec("KEY_6", "Phím Số 6 (Súng 6)", "6", "6", "Digit6", 54)

    // Function Keys F1..F8
    val KEY_F1 = PcKeySpec("KEY_F1", "Phím F1 (Điện thoại/Menu)", "F1", "F1", "F1", 112)
    val KEY_F2 = PcKeySpec("KEY_F2", "Phím F2 (Túi đồ/Hud)", "F2", "F2", "F2", 113)
    val KEY_F3 = PcKeySpec("KEY_F3", "Phím F3 (Emote/Hành động)", "F3", "F3", "F3", 114)
    val KEY_F5 = PcKeySpec("KEY_F5", "Phím F5 (Menu Job)", "F5", "F5", "F5", 116)
    val KEY_F6 = PcKeySpec("KEY_F6", "Phím F6 (Menu Gang/Ngành)", "F6", "F6", "F6", 117)
    val KEY_F8 = PcKeySpec("KEY_F8", "Phím F8 (Console)", "F8", "F8", "F8", 119)

    val ALL_KEYS: List<PcKeySpec> = listOf(
        MOUSE_LEFT, MOUSE_RIGHT, MOUSE_MIDDLE, WHEEL_UP, WHEEL_DOWN,
        KEY_SPACE, KEY_SHIFT, KEY_CTRL, KEY_ALT,
        KEY_F, KEY_E, KEY_R, KEY_Q, KEY_G, KEY_C, KEY_V, KEY_X, KEY_Z, KEY_B, KEY_N, KEY_H,
        KEY_TAB, KEY_M, KEY_T, KEY_Y, KEY_U, KEY_I, KEY_P, KEY_K, KEY_L,
        KEY_ESC, KEY_ENTER, KEY_BACKSPACE,
        KEY_UP, KEY_DOWN, KEY_LEFT, KEY_RIGHT,
        KEY_1, KEY_2, KEY_3, KEY_4, KEY_5, KEY_6,
        KEY_F1, KEY_F2, KEY_F3, KEY_F5, KEY_F6, KEY_F8,
        KEY_W, KEY_A, KEY_S, KEY_D
    )

    val QUICK_TOP_BAR_KEYS: List<PcKeySpec> = listOf(
        KEY_ESC, KEY_TAB, KEY_1, KEY_2, KEY_3, KEY_4, KEY_5,
        KEY_F1, KEY_F2, KEY_F3, KEY_F5, KEY_I, KEY_M, KEY_P, KEY_T
    )

    fun findById(id: String): PcKeySpec =
        ALL_KEYS.find { it.id == id } ?: KEY_SPACE
}

enum class ActionSlot(
    val slotId: String,
    val titleVi: String,
    val shortBadge: String,
    val defaultKeyId: String
) {
    FIRE("slot_fire", "Nút Bắn / Click Chuột Trái", "BẮN", PcKeyCatalog.MOUSE_LEFT.id),
    AIM("slot_aim", "Nút Ngắm / Chuột Phải", "NGẮM", PcKeyCatalog.MOUSE_RIGHT.id),
    JUMP("slot_jump", "Nhảy / Phanh Tay", "SPACE", PcKeyCatalog.KEY_SPACE.id),
    SPRINT("slot_sprint", "Chạy Nhanh / Nitro", "SHIFT", PcKeyCatalog.KEY_SHIFT.id),
    CROUCH("slot_crouch", "Ngồi / Rón Rén", "CTRL", PcKeyCatalog.KEY_CTRL.id),
    VEHICLE("slot_vehicle", "Lên / Xuống Xe", "XE (F)", PcKeyCatalog.KEY_F.id),
    INTERACT("slot_interact", "Tương Tác / Còi", "DÙNG (E)", PcKeyCatalog.KEY_E.id),
    RELOAD("slot_reload", "Nạp Đạn / Đánh", "NẠP (R)", PcKeyCatalog.KEY_R.id),
    COVER("slot_cover", "Ẩn Nấp / Radio", "NẤP (Q)", PcKeyCatalog.KEY_Q.id),
    GRENADE("slot_grenade", "Lựu Đạn / Dây An Toàn", "PHÍM G", PcKeyCatalog.KEY_G.id),
    HANDS_UP("slot_hands_up", "Giơ Tay / Huỷ", "PHÍM X", PcKeyCatalog.KEY_X.id),
    LOOK_BACK("slot_look_back", "Nhìn Sau / Ngồi", "PHÍM C", PcKeyCatalog.KEY_C.id),
    WEAPON("slot_weapon", "Vòng Vũ Khí", "TAB", PcKeyCatalog.KEY_TAB.id),
    CAMERA("slot_camera", "Đổi Góc Nhìn", "CAM (V)", PcKeyCatalog.KEY_V.id),
    ALT_EYE("slot_alt_eye", "Mắt Thần / Alt", "ALT", PcKeyCatalog.KEY_ALT.id),
    VOICE_N("slot_voice_n", "Đàm Thoại Mic", "MIC (N)", PcKeyCatalog.KEY_N.id),
    PHONE("slot_phone", "Mở Điện Thoại GTA", "↑ PHONE", PcKeyCatalog.KEY_UP.id),
    MENU_M("slot_menu_m", "Menu Nhân Vật", "MENU (M)", PcKeyCatalog.KEY_M.id),
    INVENTORY_I("slot_inv_i", "Túi Đồ / Inventory", "TÚI (I)", PcKeyCatalog.KEY_I.id),
    PAUSE("slot_pause", "Bản Đồ / Tạm Dừng", "ESC", PcKeyCatalog.KEY_ESC.id)
}

data class ServerPreset(
    val id: String,
    val name: String,
    val url: String,
    val badge: String,
    val description: String
)

val DEFAULT_SERVER_PRESETS = listOf(
    ServerPreset(
        id = "fiveplay_main",
        name = "FivePlay.lol (Máy Chủ Chính)",
        url = "https://fiveplay.lol",
        badge = "CHÍNH THỨC",
        description = "Cổng chơi GTA 5 WebGL trực tuyến gốc với đầy đủ bản đồ và xe cộ."
    ),
    ServerPreset(
        id = "fiveplay_fast",
        name = "FivePlay.lol (Chế Độ Tối Ưu FPS)",
        url = "https://fiveplay.lol/?mobile_boost=1&low_fx=1",
        badge = "MƯỢT HƠN",
        description = "Tự động kích hoạt giả lập Pointer Lock & tối ưu bộ đệm khung hình cho điện thoại."
    )
)
