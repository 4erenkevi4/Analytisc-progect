package com.cher.analytics.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.Top
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import coil.compose.rememberAsyncImagePainter
import com.cher.analytics.R
import com.cher.analytics.data.CardInfo
import com.cher.analytics.domain.FollowersDao
import com.cher.analytics.fragments.view.ModalBottomSheetView
import com.cher.analytics.utils.AutentificationClient
import com.cher.analytics.utils.Constants
import com.cher.analytics.utils.Constants.Companion.FOLOWERS_MODE_KEY
import com.cher.analytics.utils.Constants.Companion.PROFILE_KEY
import com.cher.analytics.utils.InstaAnalyticsUtils
import com.github.instagram4j.instagram4j.IGClient
import com.github.instagram4j.instagram4j.models.user.Profile
import com.github.instagram4j.instagram4j.models.user.User
import com.github.instagram4j.instagram4j.requests.users.UsersInfoRequest
import com.github.instagram4j.instagram4j.requests.users.UsersUsernameInfoRequest
import com.github.instagram4j.instagram4j.responses.IGResponse
import org.koin.android.ext.android.inject


class FragmentSelfProfile : FragmentCompose() {

    private var selfProfile: User? = null

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val context = context ?: return
        topBar.isVisible = false
        (arguments?.getSerializable(PROFILE_KEY) as User?)?.let {
            selfProfile = it
            viewModelBase.checkListFollowersInSP(context, it.follower_count)
        }
        getIGClient(context)?.let {
            viewModelBase.getFollowers(it, context, ifNeedSave = false, ifNeedAllList = false)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @Throws(Exception::class)
    fun testCurrent(client: IGClient) {
        val req = UsersUsernameInfoRequest(client.selfProfile.username)
        val req2 = UsersInfoRequest(client.selfProfile.pk)
        val response: IGResponse = client.sendRequest(req).join()
        val res: IGResponse = client.sendRequest(req2).join()
    }

    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    override fun Greeting() {
        val sheetState = remember{ mutableStateOf(false)}

        val context = context ?: return
        val followersListState = viewModelBase.getFollowers.observeAsState()
        val ballanceFolowersState = viewModelBase.ballanceFollowers.observeAsState()

        selfProfile?.let { selfProfile ->
            val isInterestedAccount: Boolean =
                (selfProfile.extraProperties["is_interest_account"] ?: true) as Boolean
            Scaffold() {
                Column() {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_visibility_off_24),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                        Text(
                            text = selfProfile.username,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .align(CenterVertically),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Image(
                            painter = painterResource(id = R.drawable.ic_baseline_menu_24),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 16.dp)
                        )
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Image(
                            painter = rememberAsyncImagePainter(selfProfile.hd_profile_pic_url_info.url), //TODO rememberAsyncImagePainter
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .padding(16.dp)
                        )
                        Column(
                            Modifier
                                .padding(start = 16.dp)
                                .weight(1f), horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                text = selfProfile.media_count.toString(),
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .align(CenterHorizontally)
                                    .clickable {
                                        sheetState.value =true
                                    },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Публикации")
                        }
                        Column(
                            Modifier
                                .padding(start = 16.dp)
                                .weight(1f), horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                // text = followersListState.value?.size.toString(),
                                text = selfProfile.follower_count.toString(),
                                modifier = Modifier
                                    .clickable {
                                        findNavController().navigate(
                                            R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                            bundleOf(FOLOWERS_MODE_KEY to true)
                                        )
                                    }
                                    .padding(top = 16.dp)
                                    .align(CenterHorizontally),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Подпичики")
                        }
                        Column(
                            Modifier
                                .padding(start = 16.dp)
                                .weight(1f), horizontalAlignment = CenterHorizontally
                        ) {
                            Text(
                                //text = followingsListState.value?.size.toString(),
                                text = selfProfile.following_count.toString(),
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .align(CenterHorizontally),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(text = "Подписки")
                        }
                    }
                    ballanceFolowersState.value?.let { CardDemo(balance = it, isInterestedAccount) }
                    if (followersListState.value.isNullOrEmpty().not())
                        LastFollowers(followersListState.value!!)

                    Row(Modifier.fillMaxWidth()) {
                        Column(
                            Modifier
                                .weight(1f)
                        ) {
                            CreateCardInfo(
                                cardInfo = CardInfo(
                                    "Новые подписчики за последние сутки",
                                    selfProfile.follower_count,
                                    R.drawable.ic_baseline_call_received_24,
                                    if ((ballanceFolowersState.value
                                            ?: 0) > 0
                                    ) ballanceFolowersState.value else 0,
                                    R.drawable.ic_baseline_hail_24,
                                    true
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to true)
                                    )
                                }
                            )
                        }
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to InstaAnalyticsUtils.UsersTypeList.LIST_FOLLOWERS)
                                    )
                                }) {
                            CreateCardInfo(
                                cardInfo = CardInfo(
                                    "Отписавшиеся за последние сутки",
                                    selfProfile.follower_count,
                                    R.drawable.ic_baseline_arrow_outward_24,
                                    if ((ballanceFolowersState.value
                                            ?: 0) < 0
                                    ) ballanceFolowersState.value else 0,
                                    R.drawable.ic_baseline_emoji_people_24,
                                    false
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to InstaAnalyticsUtils.UsersTypeList.LIST_FOLLOWINGS)
                                    )
                                }
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            CreateCardInfo(
                                CardInfo(
                                    "Я не подписан в ответ",
                                    282, // TODO взять из selfProfile по ключу
                                    generalIconResId = R.drawable.ic_baseline_hail_24,
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to false)
                                    )
                                }
                            )

                        }
                        Column(Modifier.weight(1f)) {
                            CreateCardInfo(
                                CardInfo(
                                    title = "Не подписаны в ответ",
                                    generalNumber = 45,  // TODO взять из selfProfile по ключу
                                    generalIconResId = R.drawable.ic_baseline_emoji_people_24,
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to false)
                                    )
                                }
                            )
                        }
                    }
                    Row(Modifier.fillMaxWidth()) {
                        Column(Modifier.weight(1f)) {
                            CreateCardInfo(
                                CardInfo(
                                    "Активные подписчики",
                                    35,
                                    generalIconResId = R.drawable.ic_baseline_hail_24,
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to false)
                                    )
                                }
                            )

                        }
                        Column(Modifier.weight(1f)) {
//                            viewModelBase.searchBots(
//                                getIGClient(context)!!,
//                                followersListState.value!!
//                            )
                            val bots = viewModelBase.listofBots.observeAsState()
                            CreateCardInfo(
                                CardInfo(
                                    if (bots.value == null) "Анализ количества ботов..." else "Найдено ботов:",
                                    if (bots.value == null) 0 else bots.value!!.size,
                                    generalIconResId = R.drawable.ic_baseline_emoji_people_24,
                                ),
                                onClick = {
                                    findNavController().navigate(
                                        R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                                        bundleOf(FOLOWERS_MODE_KEY to false)
                                    )
                                }
                            )
                        }
                    }
                }
                ModalBottomSheetView(sheetState, Color.Yellow, 30.dp)
            }
        }
    }

    @Composable
    fun CardDemo(balance: Int, isInterestedAccount: Boolean) {
        val interestedValue = if (isInterestedAccount) "интересный" else "не интересный"
        val subtitle =
            if (balance == 1) {
                "на Вас подписался"
            } else if (balance > 0) {
                "на Вас подписалось"
            } else if (balance == -1) {
                "от Вас отписался"
            } else "от Вас отписалось"
        val number =
            if (balance == 1) {
                "$balance новый аккаунт"
            } else if (balance > 0) {
                "$balance новых аккаунта"
            } else if (balance == -1) {
                "1  аккаунт"
            } else "$balance аккаунта"

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable { },
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                if (balance == 0) {
                    Text("За последних два дня количество Ваших подписчиков не изменилось")
                    Text("Алгоритмы Instagram определяют ваш аккаунт как $interestedValue")
                } else {
                    Text(
                        buildAnnotatedString {
                            append("За последние два дня ")
                            append(subtitle)
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W900,
                                    color = Color(0xFF4552B8)
                                )
                            ) {
                                append(number)
                            }
                        }
                    )
                    Text("Алгоритмы Instagram определяют ваш аккаунт как $interestedValue")
                }
            }
        }
    }

    @Composable
    fun LastFollowers(listFollowers: List<Profile>) {
        val list = mutableListOf<Profile>()
        if (listFollowers.size > 30) {
            for (i in 0..30) {
                list.add(i, listFollowers[i])
            }
        } else
            list.addAll(listFollowers)

        Column() {
            Row(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = if (list.size > 0) "Последние новые подписчики:" else "У вас пока нет подписчиков",
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
            Row(
                modifier = Modifier
                    .align(CenterHorizontally)
                    .fillMaxWidth()
            ) {
                LazyRow() {
                    itemsIndexed(list) { index, profile ->
                        Column(Modifier.size(56.dp, 70.dp)) {
                            Image(
                                painter = rememberAsyncImagePainter(profile.profile_pic_url),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        InstaAnalyticsUtils.getToInstagramApp(
                                            profile.username,
                                            context
                                        )
                                    }
                            )
                            Text(
                                text = profile.username,
                                fontSize = 9.sp,
                                modifier = Modifier
                                    .align(CenterHorizontally),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun CreateCardInfo(cardInfo: CardInfo, onClick: () -> Unit?) {
        Card(
            modifier = Modifier
                .padding(15.dp)
                .clickable { onClick.invoke() },
            elevation = 10.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    cardInfo.title
                )
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)) {
                    Text(
                        cardInfo.generalNumber.toString(), fontSize = 16.sp
                    )
                    cardInfo.arrowIconId?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier
                                .size(16.dp)
                                .align(Top)
                        )
                    }
                    cardInfo.secondNumber?.let {
                        Text(
                            text = it.toString(),
                            fontSize = 11.sp,
                            color = if (cardInfo.isPlusValue == true) Color.Green else Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = cardInfo.generalIconResId),
                        contentDescription = null
                    )
                }
            }

        }
    }


    ////////////////////////
    @SuppressLint("CoroutineCreationDuringComposition")
    @RequiresApi(Build.VERSION_CODES.N)
    @Composable
    fun Greetings() {
        selfProfile?.let { selfProfile ->
            if (selfProfile.username.isNullOrEmpty().not()) {
                titleTopBar.text = selfProfile.username
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(selfProfile.profile_pic_url),
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .padding(vertical = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Text(text = "${selfProfile.full_name} - вход выполнен.")
                    Button(modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.White), onClick = {
                        findNavController().navigate(
                            R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                            bundleOf(FOLOWERS_MODE_KEY to true)
                        )
                    }) {
                        Text(
                            text = "Список подписчиков",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Button(modifier = Modifier
                        .padding(top = 16.dp)
                        .background(Color.White), onClick = {
                        findNavController().navigate(
                            R.id.action_fragmentSelfProfile_to_fragmentListFolowers,
                            bundleOf(FOLOWERS_MODE_KEY to false)
                        )
                    }) {
                        Text(
                            text = "Кто отписался?",
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }


    @Composable
    private fun ProgressBar(responseState: MutableState<Boolean>) {
        if (responseState.value)
            Column(
                Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator(
                    Modifier.padding(16.dp),
                    color = Color.Magenta,
                    strokeWidth = Dp(value = 4F)
                )
                val uploadUsers = viewModelBase.uploadFollowers.observeAsState()
                Text(text = "Загружено ${uploadUsers.value} подписчиков")
            }
    }


    fun getIGClient(context: Context) = AutentificationClient.getClientFromSerialize(
        Constants.IG_CLIENT_FILE_NAME,
        Constants.COOKIE_FILE_NAME,
        context
    )


}

