package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.activities.*
import com.twoeightnine.root.xvii.background.DownloadFileService
import com.twoeightnine.root.xvii.background.longpoll.LongPollCore
import com.twoeightnine.root.xvii.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationJobIntentService
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.chats.attachments.audios.AudioAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.chats.attachments.links.LinkAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.stickers.StickersFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.fragments.AttachedFragment
import com.twoeightnine.root.xvii.chats.fragments.ChatFragment
import com.twoeightnine.root.xvii.chats.fragments.ChatInfoFragment
import com.twoeightnine.root.xvii.chats.fragments.ImportantFragment
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.dagger.modules.NetworkModule
import com.twoeightnine.root.xvii.dagger.modules.PresenterModule
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.friends.fragments.SearchUsersFragment
import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.searchmessages.fragments.SearchMessagesFragment
import com.twoeightnine.root.xvii.settings.fragments.AboutFragment
import com.twoeightnine.root.xvii.settings.fragments.SettingsFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, NetworkModule::class, PresenterModule::class])
interface AppComponent {

    //activities
    fun inject(loginActivity: LoginActivity)
    fun inject(exceptionActivity: ExceptionActivity)
    fun inject(rootActivity: RootActivity)
    fun inject(imageViewerActivity: ImageViewerActivity)
    fun inject(gifViewerActivity: GifViewerActivity)
    fun inject(pinActivity: PinActivity)

    //fragments
    fun inject(chatFragment: ChatFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(wallPostFragment: WallPostFragment)
    fun inject(searchUsersFragment: SearchUsersFragment)
    fun inject(searchMessagesFragment: SearchMessagesFragment)
    fun inject(importantFragment: ImportantFragment)
    fun inject(chatInfoFragment: ChatInfoFragment)
    fun inject(aboutFragment: AboutFragment)
    fun inject(accountsFragment: AccountsFragment)
    fun inject(attachedFragment: AttachedFragment)
    fun inject(friendsFragment: FriendsFragment)
    fun inject(dialogsFragment: DialogsFragment)
    fun inject(dialogsForwardFragment: DialogsForwardFragment)
    fun inject(settingsFragment: SettingsFragment)
    fun inject(docAttachmentsFragment: DocAttachmentsFragment)
    fun inject(linkAttachmentsFragment: LinkAttachmentsFragment)
    fun inject(videoAttachmentsFragment: VideoAttachmentsFragment)
    fun inject(photoAttachmentsFragment: PhotoAttachmentsFragment)
    fun inject(audioAttachmentsFragment: AudioAttachmentsFragment)
    fun inject(photoAttachFragment: PhotoAttachFragment)
    fun inject(galleryFragment: GalleryFragment)
    fun inject(docAttachFragment: DocAttachFragment)
    fun inject(videoAttachFragment: VideoAttachFragment)
    fun inject(stickersFragment: StickersFragment)

    //other
    fun inject(notificationService: NotificationService)
    fun inject(downloadFileService: DownloadFileService)
    fun inject(notfJobIntentService: NotificationJobIntentService)
    fun inject(longPollCore: LongPollCore)
    fun inject(chatFragmentPresenter: ChatFragmentPresenter)
    fun inject(markAsReadBroadcastReceiver: MarkAsReadBroadcastReceiver)

}