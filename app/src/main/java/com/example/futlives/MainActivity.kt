package com.example.futlives

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.rememberDismissState
import androidx.compose.material.rememberDrawerState
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.futlives.ui.theme.FutLivesTheme
import com.example.futlives.ui.theme.SoccerApi
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FutLivesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MyApp()
                }
            }
        }
    }
}

object MainDestinations {
    const val LOGIN_ROUTE = "login"
    const val HOME_ROUTE = "home"
    const val NOTIFICATION_ROUTE = "notificaciones"
    const val LIVE_ROUTE = "directo"
}

@Composable
fun MyApp() {
    var showDialog by remember { mutableStateOf(false) }
    var dialogMessage by remember { mutableStateOf("") }
    val selectedTeams = remember { mutableStateOf(listOf<Team>()) }
    val selectedLeagues = remember { mutableStateOf(listOf<League>()) }


    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Mensaje") },
            text = { Text(text = dialogMessage) },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    val navController = rememberNavController()
    NavHost(navController, startDestination = MainDestinations.LOGIN_ROUTE) {
        composable(MainDestinations.LOGIN_ROUTE) {
            LoginScreen(navController) { message ->
                dialogMessage = message
                showDialog = true
            }
        }
        composable(MainDestinations.LIVE_ROUTE) {
            InfoDirecto(navController)
        }


        composable(MainDestinations.HOME_ROUTE) {
            LeaguesScreen(navController, selectedLeagues)
        }

        composable(MainDestinations.NOTIFICATION_ROUTE) {
            Notificaciones(navController,selectedTeams, selectedLeagues)
        }

        composable("teams/{leagueId}") { backStackEntry ->
            TeamsScreen(navController, backStackEntry.arguments?.getString("leagueId"),selectedTeams)
        }
        composable("players/{competitionId}/{teamId}") { backStackEntry ->
            PlayersScreen(navController, backStackEntry.arguments?.getString("competitionId"), backStackEntry.arguments?.getString("teamId"))
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController, showMessage: (String) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF1a1a1a)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = if (isSignUp) "Registrarse" else "Iniciar sesión", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { newEmail -> email = newEmail }, label = { Text("Correo electrónico", color = Color.White) }, singleLine = true, modifier = Modifier.fillMaxWidth(0.8f), colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, cursorColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White))
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = password, onValueChange = { newPassword -> password = newPassword }, label = { Text("Contraseña", color = Color.White) }, singleLine = true, modifier = Modifier.fillMaxWidth(0.8f), colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, cursorColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White))
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                if (isSignUp) signUp(email, password, showMessage)
                else signIn(email, password, showMessage, navController)
            }, colors = ButtonDefaults.buttonColors(containerColor = if (isSignUp) Color(0xFF2E7D32) else Color(0xFF4CAF50)), modifier = Modifier.fillMaxWidth(0.8f)) {
                Text(if (isSignUp) "Regístrate" else "Iniciar sesión", color = Color.White)
            }
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { isSignUp = !isSignUp }) {
                Text(text = if (isSignUp) buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = Color.White)) {
                        append("¿Ya tienes cuenta? Iniciar sesión")
                    }
                } else buildAnnotatedString {
                    withStyle(style = SpanStyle(textDecoration = TextDecoration.Underline, color = Color.White)) {
                        append("¿No tienes cuenta? Regístrate")
                    }
                })
            }
        }
    }
}

fun signIn(email: String, password: String, showMessage: (String) -> Unit, navController: NavController) {
    if (email.isEmpty() || password.isEmpty()) {
        showMessage("Por favor, rellena todos los campos.")
        return
    }
    if (!email.contains("@") || !email.contains(".")) {
        showMessage("Por favor, introduce un correo electrónico válido.")
        return
    }

    val auth = FirebaseAuth.getInstance()
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showMessage("Bienvenido de nuevo")
                navController.navigate(MainDestinations.HOME_ROUTE)
            } else {
                showMessage("Hubo un error al iniciar sesión, comprueba tus credenciales")
            }
        }
}

fun signUp(email: String, password: String, showMessage: (String) -> Unit) {
    if (email.isEmpty() || password.isEmpty()) {
        showMessage("Por favor, rellena todos los campos.")
        return
    }
    if (!email.contains("@") || !email.contains(".")) {
        showMessage("Por favor, introduce un correo electrónico válido.")
        return
    }

    val auth = FirebaseAuth.getInstance()
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showMessage("Bienvenido a FutLives, inicia sesión para acceder")
            } else {
                showMessage("Hubo un error al registrarse")
            }
        }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LeaguesScreen(navController: NavController, selectedLeagues: MutableState<List<League>>) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val leagues = remember { mutableStateOf(listOf<League>()) }
    val searchQuery = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        leagues.value = getLeagues()
    }

    val filteredLeagues = leagues.value.filter { it.name.contains(searchQuery.value, ignoreCase = true) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        drawerContent = {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Información",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.HOME_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Accede a la información sobre las ligas", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Partidos en Directo",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.LIVE_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Ver información de partidos en directo (Premiere League de Ghana)", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notificaciones",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.NOTIFICATION_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Gestiona tus notificaciones", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a)), contentAlignment = Alignment.TopCenter) {
            Column() {
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, cursorColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White)
                )

                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(filteredLeagues) { league ->
                        Row(modifier = Modifier
                            .clickable {
                                scope.launch {
                                    navController.navigate("teams/${league.id}")
                                }
                            }
                            .padding(10.dp)
                            .fillMaxWidth()) {
                            Checkbox(
                                checked = selectedLeagues.value.contains(league),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedLeagues.value = selectedLeagues.value + league
                                    } else {
                                        selectedLeagues.value = selectedLeagues.value - league
                                    }
                                }
                            )
                            Column {
                                Text("${league.area}", color = Color.White)
                                Text("${league.name}", color = Color.White)
                            }
                        }
                    }
                }

            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun TeamsScreen(navController: NavController, leagueId: String?, selectedTeams: MutableState<List<Team>>) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val teams = remember { mutableStateOf(listOf<Team>()) }
    val searchQuery = remember { mutableStateOf("") }

    LaunchedEffect(leagueId) {
        teams.value = getTeams(leagueId ?: "")
    }

    val filteredTeams = teams.value.filter { it.name.contains(searchQuery.value, ignoreCase = true) }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        drawerContent = {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Información",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.HOME_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Accede a la información sobre las ligas", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Partidos en Directo",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.LIVE_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Ver información de partidos en directo (Premiere League de Ghana)", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notificaciones",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.NOTIFICATION_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Gestiona tus notificaciones", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a)), contentAlignment = Alignment.TopCenter) {
            Column() {
                OutlinedTextField(
                    value = searchQuery.value,
                    onValueChange = { searchQuery.value = it },
                    label = { Text("Buscar") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(textColor = Color.White, cursorColor = Color.White, focusedBorderColor = Color.White, unfocusedBorderColor = Color.White, focusedLabelColor = Color.White, unfocusedLabelColor = Color.White)
                )

                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(filteredTeams) { team ->
                        Row(modifier = Modifier
                            .clickable {
                                scope.launch {
                                    navController.navigate("players/${leagueId}/${team.idteam}")
                                }
                            }
                            .padding(10.dp)
                            .fillMaxWidth()) {
                            Checkbox(
                                checked = selectedTeams.value.contains(team),
                                onCheckedChange = { isChecked ->
                                    if (isChecked) {
                                        selectedTeams.value = selectedTeams.value + team
                                    } else {
                                        selectedTeams.value = selectedTeams.value - team
                                    }
                                }
                            )
                            AsyncImage(
                                model = team.iconteam,
                                contentDescription = "Logo del equipo",
                                modifier = Modifier.size(50.dp)
                            )
                            Column(modifier = Modifier.padding(start = 10.dp)) {
                                Text("${team.areateam}", color = Color.White)
                                Text("${team.name}", color = Color.White)
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PlayersScreen(navController: NavController, competitionId: String?, teamId: String?) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val players = remember { mutableStateOf(listOf<Player>()) }

    LaunchedEffect(competitionId, teamId) {
        players.value = getPlayers(competitionId ?: "", teamId ?: "")
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        drawerContent = {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Información",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.HOME_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Accede a la información sobre las ligas", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Partidos en Directo",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.LIVE_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Ver información de partidos en directo (Premiere League de Ghana)", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notificaciones",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.NOTIFICATION_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Gestiona tus notificaciones", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1a1a1a)), contentAlignment = Alignment.Center) {
            LazyColumn {
                items(players.value) { player ->
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)) {
                        AsyncImage(
                            model = player.photoplay,
                            contentDescription = "Foto del jugador",
                            modifier = Modifier.size(50.dp)
                        )
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text("${player.name}", color = Color.White)
                            Text("Número: ${player.num}", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

suspend fun getPlayers(competitionId: String, TeamId: String): List<Player> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sportsdata.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(SoccerApi::class.java)

    return service.getPlayers(competitionId, TeamId)
}

suspend fun getTeams(competitionId: String): List<Team> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sportsdata.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(SoccerApi::class.java)

    return service.getTeams(competitionId)
}


suspend fun getLeagues(): List<League> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.sportsdata.io/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(SoccerApi::class.java)

    return service.getLeagues()
}

data class League(
    @SerializedName("Name") val name: String,
    @SerializedName("AreaName") val area: String,
    @SerializedName("CompetitionId") val id: String
)

data class Team(
    @SerializedName("Name") val name: String,
    @SerializedName("AreaName") val areateam: String,
    @SerializedName("WikipediaLogoUrl") val iconteam: String,
    @SerializedName("TeamId") val idteam: String
)

data class Player(
    @SerializedName("ShortName") val name: String,
    @SerializedName("Jersey") val num: String,
    @SerializedName("PhotoUrl") val photoplay: String,
)

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class,
    ExperimentalFoundationApi::class
)
@Composable
fun Notificaciones(navController: NavController, selectedTeams: MutableState<List<Team>>, selectedLeagues: MutableState<List<League>> ) {
    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()
    val selectedTab = remember { mutableStateOf(0) }
    val tabList = listOf("Equipos", "Ligas")

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        drawerContent = {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Información",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.HOME_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Accede a la información sobre las ligas", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Partidos en Directo",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.LIVE_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Ver información de partidos en directo (Premiere League de Ghana)", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notificaciones",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.NOTIFICATION_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Gestiona tus notificaciones", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ) {
        Column {
            TabRow(
                selectedTabIndex = selectedTab.value,
                backgroundColor = Color(0xFF4CAF50),
                contentColor = Color.White,
            ) {
                tabList.forEachIndexed { index, title ->
                    Tab(
                        text = { Text(title) },
                        selected = selectedTab.value == index,
                        onClick = { selectedTab.value = index }
                    )
                }
            }

            when (selectedTab.value) {
                0 -> {
                    LazyColumn {
                        items(selectedTeams.value) { team ->
                            val dismissState = rememberDismissState()
                            SwipeToDismiss(
                                state = dismissState,
                                directions = setOf(DismissDirection.StartToEnd),
                                dismissThresholds = { FractionalThreshold(0.5f) },
                                background = { },
                                dismissContent = {
                                    Row(modifier = Modifier
                                        .padding(10.dp)
                                        .fillMaxWidth()) {
                                        AsyncImage(
                                            model = team.iconteam,
                                            contentDescription = "Logo del equipo",
                                            modifier = Modifier.size(50.dp)
                                        )
                                        Column(modifier = Modifier.padding(start = 10.dp)) {
                                            Text("${team.areateam}", color = Color.White)
                                            Text("${team.name}", color = Color.White)
                                        }
                                    }
                                }
                            )
                            LaunchedEffect(dismissState.isDismissed(DismissDirection.StartToEnd)) {
                                if (dismissState.isDismissed(DismissDirection.StartToEnd)) {
                                    selectedTeams.value = selectedTeams.value - team
                                }
                            }
                        }
                    }
                }
                1 -> {
                    LazyColumn {
                        items(selectedLeagues.value) { league ->
                            Row(modifier = Modifier
                                .padding(10.dp)
                                .fillMaxWidth()) {
                                Column {
                                    Text("${league.area}", color = Color.White)
                                    Text("${league.name}", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun InfoDirecto(navController: NavController) {
    val liveScores = remember { mutableStateOf(listOf<LiveScoreResult>()) }
    val errorMessage = remember { mutableStateOf<String?>(null) }
    val selectedMatch = remember { mutableStateOf<LiveScoreResult?>(null) }

    val scaffoldState = rememberScaffoldState(rememberDrawerState(DrawerValue.Closed))
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            liveScores.value = getLiveScores()
        } catch (e: Exception) {
            errorMessage.value = "No se encontraron partidos en vivo"
        }
    }

    Scaffold(
        scaffoldState = scaffoldState,
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { scaffoldState.drawerState.open() } }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
                    }
                }
            )
        },
        drawerContent = {
            val scope = rememberCoroutineScope()
            val context = LocalContext.current

            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Top) {
                Text(
                    text = "Información",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.HOME_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Accede a la información sobre las ligas", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Partidos en Directo",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.LIVE_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Ver información de partidos en directo (Premiere League de Ghana)", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Notificaciones",
                    modifier = Modifier
                        .padding(16.dp)
                        .combinedClickable(
                            onClick = { navController.navigate(MainDestinations.NOTIFICATION_ROUTE) },
                            onLongClick = { scope.launch { Toast.makeText(context, "Gestiona tus notificaciones", Toast.LENGTH_SHORT).show() } }
                        ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (errorMessage.value != null) {
                Text(
                    text = errorMessage.value!!,
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(16.dp)
                )
            } else if (selectedMatch.value == null) {
                LazyColumn {
                    items(liveScores.value) { score ->
                        Row(modifier = Modifier
                            .combinedClickable(onClick = {}, onLongClick = { selectedMatch.value = score })) {
                            AsyncImage(
                                model = score.leagueLogo,
                                contentDescription = "Logo de la liga",
                                modifier = Modifier.size(50.dp)
                            )
                            Column {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Fecha: ${score.eventDate} ${score.eventTime}", color = Color.White)
                                Text("Equipo local: ${score.homeTeam}", color = Color.White)
                                Text("Equipo visitante: ${score.awayTeam}", color = Color.White)
                                Text("Resultado final: ${score.finalResult}", color = Color.White)
                                Text("Estadio: ${score.stadium}", color = Color.White)
                            }
                        }
                    }
                }
            }else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (selectedMatch.value!!.goalScorers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Goles:", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        for (scorer in selectedMatch.value!!.goalScorers) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Minuto en donde se marcó: ${scorer.time}", color = Color.White)
                            val goleador = if (scorer.homeScorer.isNotEmpty()) scorer.homeScorer else scorer.awayScorer
                            Text("Goleador: $goleador", color = Color.White)
                            Text("Resultado: ${scorer.score}", color = Color.White)
                        }
                    }
                    if (selectedMatch.value!!.cards.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Tarjetas:", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        for (card in selectedMatch.value!!.cards) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tiempo: ${card.time}", color = Color.White)
                            val falta = if (card.homeFault.isNotEmpty()) card.homeFault else card.awayFault
                            Text("Falta: $falta", color = Color.White)
                            Text("Tarjeta: ${card.card}", color = Color.White)
                        }
                    }

                    if (selectedMatch.value!!.substitutes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(32.dp))
                        Text("Sustituciones:", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        for (substitute in selectedMatch.value!!.substitutes) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Tiempo: ${substitute.time}", color = Color.White)
                            Text("Jugador que entra: ${substitute.homeScorer["in"]}", color = Color.White)
                            Text("Jugador que sale: ${substitute.homeScorer["out"]}", color = Color.White)
                            Text("Tipo de cambio: ${substitute.score}", color = Color.White)
                        }
                    }

                    Button(onClick = { selectedMatch.value = null }) {
                        Text("Volver a la lista de partidos")
                    }
                }
            }
        }
    }
}

data class GoalScorer(
    @SerializedName("time") val time: String,
    @SerializedName("home_scorer") val homeScorer: String,
    @SerializedName("score") val score: String,
    @SerializedName("away_scorer") val awayScorer: String
)

data class Card(
    @SerializedName("time") val time: String,
    @SerializedName("home_fault") val homeFault: String,
    @SerializedName("card") val card: String,
    @SerializedName("away_fault") val awayFault: String
)

data class Substitute(
    @SerializedName("time") val time: String,
    @SerializedName("home_scorer") val homeScorer: Map<String, String>,
    @SerializedName("score") val score: String,
    @SerializedName("away_scorer") val awayScorer: List<String>
)

data class LiveScoreResult(
    @SerializedName("event_date") val eventDate: String,
    @SerializedName("event_time") val eventTime: String,
    @SerializedName("event_home_team") val homeTeam: String,
    @SerializedName("event_away_team") val awayTeam: String,
    @SerializedName("event_final_result") val finalResult: String,
    @SerializedName("event_stadium") val stadium: String,
    @SerializedName("league_logo") val leagueLogo: String,
    @SerializedName("goalscorers") val goalScorers: List<GoalScorer>,
    @SerializedName("cards") val cards: List<Card>,
    @SerializedName("substitutes") val substitutes: List<Substitute>
)

data class LiveScoreResponse(
    @SerializedName("success") val success: Int,
    @SerializedName("result") val result: List<LiveScoreResult>
)


suspend fun getLiveScores(): List<LiveScoreResult> {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://apiv2.allsportsapi.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(SoccerApi::class.java)
    val response = service.getLiveScores()

    if (response.result.isEmpty()) {
        throw Exception("No se encontraron partidos en vivo")
    }

    return response.result
}

val Green200 = Color(0xFFA5D6A7)
val Green500 = Color(0xFF4CAF50)
val Green700 = Color(0xFF388E3C)
val Green900 = Color(0xFF2E7D32)