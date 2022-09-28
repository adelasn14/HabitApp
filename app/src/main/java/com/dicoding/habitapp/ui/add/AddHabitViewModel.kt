package com.dicoding.habitapp.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dicoding.habitapp.data.Habit
import com.dicoding.habitapp.data.HabitRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddHabitViewModel(private val habitRepository: HabitRepository) : ViewModel() {
    fun saveHabit(habit: Habit) {
        // need to be async so we use coroutinescope
        viewModelScope.launch(Dispatchers.IO) {
            habitRepository.insertHabit(habit)
        }
    }
}