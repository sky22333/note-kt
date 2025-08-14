package com.noteapp.modernotes.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.noteapp.modernotes.ui.screens.NoteEditScreen
import com.noteapp.modernotes.ui.screens.NotesScreen

/**
 * 应用导航路由
 */
object NoteDestinations {
    const val NOTES_ROUTE = "notes"
    const val NOTE_EDIT_ROUTE = "note_edit"
    const val NOTE_ID_KEY = "noteId"
}

/**
 * 应用导航组件
 */
@Composable
fun NoteNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = NoteDestinations.NOTES_ROUTE
    ) {
        // 笔记列表页面
        composable(NoteDestinations.NOTES_ROUTE) {
            NotesScreen(
                onNavigateToEdit = { noteId ->
                    navController.navigate("${NoteDestinations.NOTE_EDIT_ROUTE}/$noteId")
                }
            )
        }
        
        // 笔记编辑页面
        composable("${NoteDestinations.NOTE_EDIT_ROUTE}/{${NoteDestinations.NOTE_ID_KEY}}") { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString(NoteDestinations.NOTE_ID_KEY)?.toLongOrNull() ?: 0L
            
            NoteEditScreen(
                noteId = noteId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}