package org.fg.ttrpg.timeline

import java.util.UUID

class TimelineEvent {
    var id: UUID? = null
    var calendar: org.fg.ttrpg.calendar.CalendarSystem? = null
    var title: String? = null
    var description: String? = null
    var startDay: Int? = null
    var endDay: Int? = null
    var objectRefs: MutableList<org.fg.ttrpg.setting.SettingObject> = mutableListOf()
    var tags: MutableList<String> = mutableListOf()
}
