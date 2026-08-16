# APK Forge

APK Forge is an AI‑powered Android toolkit for analyzing, inspecting, verifying, and installing APK files.  
It integrates directly with an AI Stodio backend to provide intelligent insights into package structure, metadata, and safety.

## Features
- 🔍 APK metadata extraction  
- 🛡️ Safety scanning and verification  
- 📦 Manifest + package info  
- 🤖 AI-assisted analysis via AI Stodio  
- 📱 Install APKs directly from the app  
- ⚙️ Developer utilities for modding and debugging  

## Project Structure
- **app/** — Main Android application module  
- **assets/.aistudio/** — AI Stodio configuration and metadata  
- **gradle/** — Gradle wrapper and build system  
- **build.gradle.kts** — Module build configuration  
- **settings.gradle.kts** — Project module definitions  
- **metadata.json** — AI Stodio metadata  
- **.env.example** — Environment variable template  
- **.gitignore** — Ignored files and build artifacts  

## AI Backend
APK Forge connects to a deployed AI Stodio endpoint:

https://ais-dev-a6u7rg2xhdnkwxufqkdld5-112055216493.europe-west1.run.app

## Roadmap
- Signature verification  
- Manifest editor  
- Resource browser  
- Smali viewer  
- On-device AI model
