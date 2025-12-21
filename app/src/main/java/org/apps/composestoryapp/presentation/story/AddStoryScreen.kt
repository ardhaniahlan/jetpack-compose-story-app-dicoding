package org.apps.composestoryapp.presentation.story

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import org.apps.composestoryapp.ViewState
import org.apps.composestoryapp.getCurrentLocation
import org.apps.composestoryapp.presentation.home.StoryState
import org.apps.composestoryapp.ui.theme.ComposeStoryAppTheme
import org.apps.composestoryapp.ui.theme.DarkGreenPrimary
import org.apps.composestoryapp.ui.theme.GreyLight
import java.io.File

@Composable
fun AddStoryScreen(
    navController: NavController,
    storyViewModel: StoryViewModel = hiltViewModel()
){
    val context = LocalContext.current
    val uiState by storyViewModel.uiState.collectAsState()
    var tempUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            storyViewModel.setImageUri(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            storyViewModel.setImageUri(uri = tempUri)
        }
    }

    val launchCamera: () -> Unit = {
        val photoFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "photo_${System.currentTimeMillis()}.jpg"
        )

        val photoUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            photoFile
        )
        tempUri = photoUri
        cameraLauncher.launch(photoUri)
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation(
                context = context,
                onResult = { lat, lon ->
                    storyViewModel.setLocation(lat, lon)
                },
                onError = {
                    Toast.makeText(
                        context,
                        "Gagal mendapatkan lokasi",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        } else {
            storyViewModel.setUseLocation(false)
            Toast.makeText(
                context,
                "Lokasi tidak diizinkan",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onUseLocationChange: (Boolean) -> Unit = { checked ->
        storyViewModel.setUseLocation(checked)

        if (checked) {
            val permission = Manifest.permission.ACCESS_FINE_LOCATION
            if (
                ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                getCurrentLocation(
                    context = context,
                    onResult = { lat, lon ->
                        storyViewModel.setLocation(lat, lon)
                    },
                    onError = {
                        Toast.makeText(
                            context,
                            "Gagal mendapatkan lokasi",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )

            } else {
                locationPermissionLauncher.launch(permission)
            }
        }
    }

    LaunchedEffect(uiState.addStoryState) {
        when (uiState.addStoryState) {
            is ViewState.Success -> {
                Toast.makeText(context, "Story uploaded!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            is ViewState.Error -> {
                val error = (uiState.addStoryState as ViewState.Error).message
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            AddStoryTopAppBar(
                onResetClick = {
                    storyViewModel.clearForm()
                },
                onSaveClick = {
                    storyViewModel.addStory()
                },
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AddStoryContent(
                uiState = uiState,
                onDescriptionChange = { storyViewModel.onDescriptionChange(it) },
                onPickImageClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                onCameraClick = {
                    val permission = Manifest.permission.CAMERA
                    if (ContextCompat.checkSelfPermission(
                            context,
                            permission
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        launchCamera()
                    } else {
                        cameraPermissionLauncher.launch(permission)
                    }
                },
                onUseLocationChange = onUseLocationChange
            )

            val isActionLoading = uiState.addStoryState is ViewState.Loading

            if (isActionLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .pointerInput(Unit) {
                            detectTapGestures { }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStoryTopAppBar(
    onResetClick: () -> Unit,
    onSaveClick: () -> Unit,
){
    TopAppBar(
        title = {
            Text(
                text = "Add Post",
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        },
        actions = {
            TextButton(
                onClick = onResetClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color.Red
                ),
            ) {
                Text(
                    text = "Reset",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = onSaveClick,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
            ) {
                Text(
                    text = "Upload",
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = Color.White
        ),
    )
}

@Composable
fun AddStoryContent(
    uiState: StoryState,
    onDescriptionChange: (String) -> Unit,
    onPickImageClick: () -> Unit,
    onCameraClick: () -> Unit,
    onUseLocationChange: (Boolean) -> Unit
){

    LazyColumn(
        modifier = Modifier
            .padding(30.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Box(
                modifier = Modifier
                    .padding(top = 20.dp)
                    .size(200.dp)
                    .background(GreyLight)
                    .clickable(onClick = onPickImageClick),
                contentAlignment = Alignment.Center
            ) {
                if (uiState.photoFile != null) {
                    AsyncImage(
                        model = uiState.photoFile,
                        contentDescription = "Picked Picture",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Pick Photo",
                        modifier = Modifier.size(50.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = uiState.useLocation,
                    onCheckedChange = onUseLocationChange
                )
                Text("Sertakan lokasi saya")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onCameraClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Camera",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Camera")
                }

                Button(
                    onClick = onPickImageClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Gallery",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Gallery")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = uiState.description,
                onValueChange = onDescriptionChange,
                placeholder = { Text("Tulis story disini...") },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .border(
                        width = 1.dp,
                        color = DarkGreenPrimary,
                        shape = RoundedCornerShape(16.dp)
                    ),
                maxLines = 5,
                colors = TextFieldDefaults.colors(
                    unfocusedContainerColor = GreyLight,
                    focusedContainerColor = GreyLight,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddStoryScreenPreview(){
    ComposeStoryAppTheme {
        AddStoryContent(
            uiState = StoryState(
                description = "",
                photoFile = null,
                storyState = ViewState.Idle,
                storyListState = ViewState.Idle,
                addStoryState = ViewState.Idle,
            ),
            onDescriptionChange = {},
            onCameraClick = {},
            onPickImageClick = {},
            onUseLocationChange = {}
        )
    }
}