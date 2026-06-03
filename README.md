# Planzy
Planzy is an Android application designed to simplify the trip-planning process. It is designed to streamline the travel planning process by integrating destination discovery, itinerary organization, and personalized recommendations into a single, user-friendly platform.

## Core Objectives
The application addresses the common challenge of using multiple websites and apps simultaneously to plan a trip. It provides a centralized solution where users can research locations, build detailed schedules, and access community-driven insights.

## Key Features
- Authentication: Secure Login and Registration system for a personalized experience.
- Home Screen: A discovery hub for exploring new travel ideas and trending destinations.
- Vacation Planner & Details: The central tool for creating, managing, and viewing trip itineraries.
- Place Details: In-depth information about specific points of interest, featuring real-time data and reviews.
- History: A dedicated section for tracking and reviewing past trips and completed plans.
- Profile Management: Customizable user profiles to manage personal preferences and travel history.

## Technical Implementation
The app is built using a modern and scalable tech stack:
- Language & UI: Developed natively in Kotlin using Jetpack Compose for a modern, declarative user interface.
- Backend: Powered by Supabase, utilizing PostgreSQL for database management, secure Authentication, and Cloud Storage.
- Intelligence: Integrated Google ML Kit for on-device machine learning capabilities (such as text recognition).
- External Data: Leverages the TripAdvisor API to provide users with up-to-date ratings, reviews, and location details.
- Architecture: Follows Clean Architecture principles combined with the MVVM (Model-View-ViewModel) pattern to ensure a clear separation of concerns and easy maintenance.

## Instalation
The easiest way to test the Planzy app is by installing the pre-built APK.

**Installation Steps:**

- Download the APK: Navigate to the Release section of this repository and download the app-release.apk file.
- Allow Unknown Sources: Since this app is not on the Google Play Store, your phone will ask for permission to install apps from "Unknown Sources." Go to your browser/file manager settings and enable it.
- Install & Run: Open the file and tap Install. Once the process is complete, you can find the app in your menu.

  Note: An active internet connection is required for the app to sync with the database and fetch travel data.

