/*
 * Copyright (C) 2014 AChep@xda <artemchep@gmail.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package com.achep.activedisplay.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.achep.activedisplay.Operator;
import com.achep.activedisplay.utils.PendingIntentUtils;

/**
 * Created by Artem on 19.01.14.
 */
public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static boolean startContentIntent(OpenStatusBarNotification notification) {
        return startContentIntent(notification.getStatusBarNotification());
    }

    public static boolean startContentIntent(StatusBarNotification notification) {
        if (notification != null) {
            PendingIntent pi = notification.getNotification().contentIntent;
            boolean successful = PendingIntentUtils.sendPendingIntent(pi);
            if (successful && Operator.bitandCompare(
                    notification.getNotification().flags,
                    Notification.FLAG_AUTO_CANCEL)) {
                dismissNotification(notification);
            }
            return successful;
        }
        return false;
    }

    public static void dismissNotification(StatusBarNotification notification) {
        NotificationHandleService nhs = NotificationHandleService.sService;
        if (nhs != null) {
            nhs.cancelNotification(
                    notification.getPackageName(),
                    notification.getTag(),
                    notification.getId());
            NotificationPresenter.getInstance(nhs).removeNotification(nhs, notification);
        } else {
            Log.e(TAG, "Failed to dismiss notification due to offline notification service.");
        }
    }

}
