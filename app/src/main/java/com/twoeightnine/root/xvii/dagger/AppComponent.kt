package com.twoeightnine.root.xvii.dagger

import com.twoeightnine.root.xvii.accounts.fragments.AccountsFragment
import com.twoeightnine.root.xvii.activities.ExceptionActivity
import com.twoeightnine.root.xvii.analyzer.dialog.AnalyzeDialogFragment
import com.twoeightnine.root.xvii.background.longpoll.core.LongPollCore
import com.twoeightnine.root.xvii.background.longpoll.receivers.KeyExchangeHandler
import com.twoeightnine.root.xvii.background.longpoll.receivers.MarkAsReadBroadcastReceiver
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationJobIntentService
import com.twoeightnine.root.xvii.background.longpoll.services.NotificationService
import com.twoeightnine.root.xvii.background.messaging.MessageDestructionService
import com.twoeightnine.root.xvii.chatowner.ChatOwnerViewModel
import com.twoeightnine.root.xvii.chats.attachments.audios.AudioAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.docs.DocAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.gallery.GalleryFragment
import com.twoeightnine.root.xvii.chats.attachments.links.LinkAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.photos.PhotoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.attachments.stickers.StickersWindow
import com.twoeightnine.root.xvii.chats.attachments.stickersemoji.StickersEmojiRepository
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachFragment
import com.twoeightnine.root.xvii.chats.attachments.videos.VideoAttachmentsFragment
import com.twoeightnine.root.xvii.chats.messages.chat.base.BaseChatMessagesViewModel
import com.twoeightnine.root.xvii.chats.messages.chat.secret.SecretChatMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.chat.usual.ChatMessagesFragment
import com.twoeightnine.root.xvii.chats.messages.deepforwarded.DeepForwardedFragment
import com.twoeightnine.root.xvii.chats.messages.starred.StarredMessagesFragment
import com.twoeightnine.root.xvii.dagger.modules.ContextModule
import com.twoeightnine.root.xvii.dagger.modules.NetworkModule
import com.twoeightnine.root.xvii.dagger.modules.PresenterModule
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsForwardFragment
import com.twoeightnine.root.xvii.dialogs.fragments.DialogsFragment
import com.twoeightnine.root.xvii.features.FeaturesFragment
import com.twoeightnine.root.xvii.features.general.GeneralViewModel
import com.twoeightnine.root.xvii.friends.fragments.FriendsFragment
import com.twoeightnine.root.xvii.login.LoginActivity
import com.twoeightnine.root.xvii.login.LoginViewModel
import com.twoeightnine.root.xvii.main.MainActivity
import com.twoeightnine.root.xvii.photoviewer.ImageViewerActivity
import com.twoeightnine.root.xvii.pin.PinActivity
import com.twoeightnine.root.xvii.poll.PollFragment
import com.twoeightnine.root.xvii.scheduled.core.SendMessageWorker
import com.twoeightnine.root.xvii.scheduled.ui.ScheduledMessagesViewModel
import com.twoeightnine.root.xvii.search.SearchFragment
import com.twoeightnine.root.xvii.utils.AppLifecycleTracker
import com.twoeightnine.root.xvii.utils.DefaultPeerResolver
import com.twoeightnine.root.xvii.wallpost.WallPostFragment
import com.twoeightnine.root.xvii.web.GifViewerFragment
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [ContextModule::class, NetworkModule::class, PresenterModule::class])
interface AppComponent {

    //activities
    fun inject(loginActivity: LoginActivity)
    fun inject(mainActivity: MainActivity)
    fun inject(exceptionActivity: ExceptionActivity)
    fun inject(imageViewerActivity: ImageViewerActivity)
    fun inject(pinActivity: PinActivity)

    // fragments
    fun inject(wallPostFragment: WallPostFragment)
    fun inject(gifViewerFragment: GifViewerFragment)
    fun inject(accountsFragment: AccountsFragment)
    fun inject(friendsFragment: FriendsFragment)
    fun inject(dialogsFragment: DialogsFragment)
    fun inject(dialogsForwardFragment: DialogsForwardFragment)
    fun inject(docAttachmentsFragment: DocAttachmentsFragment)
    fun inject(linkAttachmentsFragment: LinkAttachmentsFragment)
    fun inject(videoAttachmentsFragment: VideoAttachmentsFragment)
    fun inject(photoAttachmentsFragment: PhotoAttachmentsFragment)
    fun inject(audioAttachmentsFragment: AudioAttachmentsFragment)
    fun inject(photoAttachFragment: PhotoAttachFragment)
    fun inject(galleryFragment: GalleryFragment)
    fun inject(docAttachFragment: DocAttachFragment)
    fun inject(videoAttachFragment: VideoAttachFragment)
    fun inject(starredMessagesFragment: StarredMessagesFragment)
    fun inject(chatMessagesFragment: ChatMessagesFragment)
    fun inject(secretChatMessagesFragment: SecretChatMessagesFragment)
    fun inject(deepForwardedFragment: DeepForwardedFragment)
    fun inject(featuresFragment: FeaturesFragment)
    fun inject(searchFragment: SearchFragment)
    fun inject(analyzeDialogFragment: AnalyzeDialogFragment)
    fun inject(pollFragment: PollFragment)

    //other
    fun inject(notificationService: NotificationService)
    fun inject(destructionService: MessageDestructionService)
    fun inject(sendMessageWorker: SendMessageWorker)
    fun inject(notfJobIntentService: NotificationJobIntentService)
    fun inject(longPollCore: LongPollCore)
    fun inject(markAsReadBroadcastReceiver: MarkAsReadBroadcastReceiver)
    fun inject(stickersWindow: StickersWindow)
    fun inject(appLifecycleTracker: AppLifecycleTracker)
    fun inject(stickersEmojiRepository: StickersEmojiRepository)
    fun inject(defaultPeerResolver: DefaultPeerResolver)
    fun inject(keyExchangeHandler: KeyExchangeHandler)

    fun inject(chatOwnerViewModel: ChatOwnerViewModel)
    fun inject(generalViewModel: GeneralViewModel)
    fun inject(baseChatMessagesViewModel: BaseChatMessagesViewModel)
    fun inject(scheduledMessagesViewModel: ScheduledMessagesViewModel)
    fun inject(loginViewModel: LoginViewModel)

}