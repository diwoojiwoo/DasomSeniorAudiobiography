package com.onethefull.dasomcontents.utils.bus

import com.onethefull.dasomcontents.utils.logger.DWLog
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/**
 * Created by Douner on 2019. 5. 8..
 */
object RxBus {
    private val publishSubject = PublishSubject.create<Any>()

    fun publish(event: Any) {

        DWLog.d("RxBus publish ==> $event")
        publishSubject.onNext(event)
    }

    fun <T> listen(eventType: Class<T>): Observable<T> = publishSubject.ofType(eventType)
}
