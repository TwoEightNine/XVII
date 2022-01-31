/*
 * xvii - messenger for vk
 * Copyright (C) 2021  TwoEightNine
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package global.msnthrp.xvii.core.report

import global.msnthrp.xvii.core.report.model.ReportReason

class ReportUseCase(
        private val reportDataSource: ReportDataSource
) {

    fun reportUser(userId: Int, reason: ReportReason, comment: String) {
        reportDataSource.reportUser(userId, reason, comment)
    }

    fun reportWallPost(ownerId: Int, postId: Int, reason: ReportReason) {
        reportDataSource.reportWallPost(ownerId, postId, reason)
    }

    fun reportPhoto(ownerId: Int, photoId: Int, reason: ReportReason) {
        reportDataSource.reportPhoto(ownerId, photoId, reason)
    }

}