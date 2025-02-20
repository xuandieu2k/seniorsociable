package vn.xdeuhug.seniorsociable.model.eventbus

import vn.xdeuhug.seniorsociable.model.entity.modelNotification.Notification


/**
 * @Author: NGUYEN XUAN DIEU
 * @Date: 20 / 12 / 2023
 */

class NotificationEventBus(var notification: Notification, var onAdded: Boolean) {} //onAdded = true Thêm vào thông báo ngược lại remove