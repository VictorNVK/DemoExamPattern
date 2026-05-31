# Windows stop scripts

Run from the **repository root** (not from `demo-exam-spring-backend`):

```powershell
.\scripts\windows\stop-backend.ps1
.\scripts\windows\stop-client.ps1
.\scripts\windows\stop-all.ps1
```

Or double-click `.bat` files in this folder.

There is no `gradlew stop` task. Use `Ctrl+C` in the terminal where `bootRun` / `run` is running, or these scripts.
