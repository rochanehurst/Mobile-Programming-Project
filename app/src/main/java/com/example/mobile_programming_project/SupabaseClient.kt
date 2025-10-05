package com.example.mobile_programming_project

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.storage.Storage

val supabase = createSupabaseClient(
    supabaseUrl = "https://egkjdwiqglvdlimzwpgv.supabase.co",
    supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImVna2pkd2lxZ2x2ZGxpbXp3cGd2Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTk2MTU5ODYsImV4cCI6MjA3NTE5MTk4Nn0.47Yl53OpRg-T8jnufcktaMu1ydjXAKxI76xZURMm0cA"
) {
    install(Storage)
}