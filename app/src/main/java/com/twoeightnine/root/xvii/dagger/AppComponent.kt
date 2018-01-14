package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.activities.ExceptionActivity
import com.twoeightnine.root.xvii.activities.GifViewerActivity
import com.twoeightnine.root.xvii.activities.LoginActivity
import com.twoeightnine.root.xvii.activities.RootActivity
import com.twoeightnine.root.xvii.background.LongPollService
import com.twoeightnine.root.xvii.background.NotificationService
import com.twoeightnine.root.xvii.chats.fragments.*
import com.twoeightnine.root.xvii.chats.fragments.attach.DocAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attach.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attach.VideoAttachFragment
import com.twoeightnine.root.xvii.chats.fragments.attachments.DocAttachmentsFragment
import com.twoeightnine.root.xvii.chats.fragments.attachments.LinkAttachmentsFragment
import com.twoeightnine.root.xvii.chats.fragments.attachments.PhotoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.fragments.attachments.VideoAttachmentsFragment
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.dagger.modules.NetworkModule
import com.twoeightnine.root.xvii.dagger.modules.PresenterModule
import com.twoeightnine.root.xvii.dialogs.fragments.DialogFwFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.dialogs.fragments.SearchMessagesFragment
import com.twoeightnine.root.xvii.feed.fragments.FeedFragment
import com.twoeightnine.root.xvii.fragments.WallPostFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsAllFragment
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.friends.fragments.SearchUsersFragment
import com.twoeightnine.root.xvii.music.MusicFragment
import com.twoeightnine.root.xvii.mvp.presenter.ChatFragmentPresenter
import com.twoeightnine.root.xvii.mvp.presenter.DialogsFragmentPresenter
import com.twoeightnine.root.xvii.profile.fragments.ProfileFragment
import com.twoeightnine.root.xvii.settings.fragments.AboutFragment
import com.twoeightnine.root.xvii.settings.fragments.AccountsFragment
import com.twoeightnine.root.xvii.utils.ApiUtils
import com.twoeightnine.root.xvii.views.photoviewer.ImageViewerActivity
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = arrayOf(ContextModule::class, NetworkModule::class, PresenterModule::class))
interface AppComponent {

    //activities
    fun inject(loginActivity: LoginActivity)
    fun inject(exceptionActivity: ExceptionActivity)
    fun inject(rootActivity: RootActivity)
    fun inject(imageViewerActivity: ImageViewerActivity)
    fun inject(gifViewerActivity: GifViewerActivity)

    //fragments
    fun inject(dialogsFragment: DialogsFragment)
    fun inject(chatFragment: ChatFragment)
    fun inject(profileFragment: ProfileFragment)
    fun inject(friendsFragment: FriendsFragment)
    fun inject(photoAttachFragment: PhotoAttachFragment)
    fun inject(docAttachFragment: DocAttachFragment)
    fun inject(videoAttachFragment: VideoAttachFragment)
    fun inject(voiceRecordFragment: VoiceRecordFragment)
    fun inject(wallPostFragment: WallPostFragment)
    fun inject(photoAttachmentsFragment: PhotoAttachmentsFragment)
    fun inject(docAttachmentsFragment: DocAttachmentsFragment)
    fun inject(linkAttachmentsFragment: LinkAttachmentsFragment)
    fun inject(videoAttachmentsFragment: VideoAttachmentsFragment)
    fun inject(friendsAllFragment: FriendsAllFragment)
    fun inject(musicFragment: MusicFragment)
    fun inject(searchUsersFragment: SearchUsersFragment)
    fun inject(searchMessagesFragment: SearchMessagesFragment)
    fun inject(importantFragment: ImportantFragment)
    fun inject(feedFragment: FeedFragment)
    fun inject(dialogFwFragment: DialogFwFragment)
    fun inject(chatInfoFragment: ChatInfoFragment)
    fun inject(aboutFragment: AboutFragment)
    fun inject(accountsFragment: AccountsFragment)
    fun inject(attachedFragment: AttachedFragment)

    //other
//    fun inject(apiUtils: ApiUtils)
    fun inject(longPollService: LongPollService)
    fun inject(notificationService: NotificationService)
    fun inject(chatFragmentPresenter: ChatFragmentPresenter)
    fun inject(dialogsFragmentPresenter: DialogsFragmentPresenter)

}