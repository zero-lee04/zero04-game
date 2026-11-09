# 🌌 별자리를 찾아서

> 잃어버린 별자리를 되찾는 여정을 담은 Java · libGDX 기반 2D 게임 프로젝트

**물병자리의 플랫폼 액션 → 물고기자리의 벽돌 깨기 → 양자리의 보스전**으로 이어지는 세 가지 게임을 구현했습니다. 각 스테이지를 완료하면 대화와 배경 전환을 통해 다음 별자리로 여정이 이어집니다.


## 🎮 게임 소개

| 스테이지 | 장르 | 목표 및 주요 요소 |
| --- | --- | --- |
| Aquarius · 물병자리 | 횡스크롤 플랫폼 액션 | 별 17개 수집 후 포털 도달, 점프·웅크리기·승강 발판 활용 |
| Pisces · 물고기자리 | 벽돌 깨기 | 패들로 공을 튕겨 별 벽돌 11개 제거 |
| Aries · 양자리 | 탄막 슈팅 보스전 | 탄환과 레이저를 피하며 보스 격파, 보스 체력이 절반 이하가 되면 공격 강화 |

### 주요 구현 내용

- **화면별 게임 로직 분리**: libGDX의 `Game`과 `Screen`을 활용한 화면 전환
- **캐릭터 애니메이션**: 대기, 이동, 점프, 기어가기 상태별 스프라이트 처리
- **플랫폼 액션**: 중력, 이동 및 충돌 판정, 스크롤 카메라, 버튼을 이용한 발판 조작
- **벽돌 깨기**: 공·패들·벽돌 충돌 처리, 점수 및 목숨 표시
- **보스전**: 플레이어·보스 체력 관리, 탄환 및 레이저 공격, 체력에 따른 공격 강화
- **연출 및 편의 기능**: 타이핑 대화, 페이드 전환, 일시정지 및 재시작


## 🎮 게임 다운로드

[Windows 설치 파일 다운로드](https://github.com/zero-lee04/zero04-game/releases/latest/download/Zero04Game-1.0.0.exe)

다운로드한 EXE를 실행해 설치한 뒤, 바탕화면의 Zero04Game 바로가기로 게임을 실행하세요.

## ⌨️ 조작 방법

### 시작 화면 / 대화

| 키 | 기능 |
| --- | --- |
| `S` | 시작 화면에서 게임 시작 |
| `Esc` | 시작 화면에서 게임 종료 |
| `Space` | 대화 표시, 현재 문장 즉시 표시 또는 다음 대화 진행 |
| `F5` | 엔딩 대화 완료 후 시작 화면으로 돌아가기 |

### Aquarius · 물병자리

| 키 | 기능 |
| --- | --- |
| `←` / `→` | 좌우 이동 |
| `Space` | 점프 |
| `↓` | 지상에서 웅크리기 / 기어가기 |
| `E` | 버튼 또는 승강 발판 근처에서 발판 조작 |
| `H` | 도움말 표시 / 숨기기 |
| `Esc` | 일시정지 / 재개 |
| `F5` | 스테이지 재시작 |

별 **17개를 모두 수집한 뒤 포털에 도달**하면 다음 단계로 진행합니다.

### Pisces · 물고기자리

| 키 | 기능 |
| --- | --- |
| `←` / `→` | 패들 이동 |
| `Space` | 공 발사 |
| `Esc` | 일시정지 / 재개 |
| `F5` | 스테이지 재시작 |

별 벽돌 **11개를 모두 제거**하면 다음 단계로 진행합니다.

### Aries · 양자리

| 키 | 기능 |
| --- | --- |
| `W` / `A` / `S` / `D` 또는 방향키 | 상하좌우 이동 |
| `Space` 길게 누르기 | 연속 공격 |
| `Esc` | 일시정지 / 재개 |
| `F5` | 보스전 재시작 |

플레이어의 초기 체력은 **5**, 보스의 초기 체력은 **100**입니다. 보스 체력이 절반 이하가 되면 공격이 강화됩니다.

## 📁 프로젝트 구조

```text
.
├── assets/                      # 배경, 캐릭터, 별, 포털 등 게임 리소스
├── core/
│   ├── src/main/java/io/jbnu/hw1/
│   │   ├── MainScreen.java      # 게임 진입점 및 최초 화면 지정
│   │   ├── GamingScreen.java    # 시작 화면
│   │   ├── FirstGameScreen.java # 스토리 대화 및 스테이지 연결
│   │   ├── AquariusGame.java    # 물병자리 플랫폼 액션
│   │   ├── PiscesGame.java      # 물고기자리 벽돌 깨기
│   │   ├── AriesGame.java       # 양자리 보스전
│   │   ├── StadingPlayer.java   # 대기 애니메이션 (원본 클래스명)
│   │   ├── RightMovingPlayer.java # 캐릭터 움직임 애니메이션
│   │   ├── JumpingPlayer.java   # 캐릭터 점프 애니메이션
│   │   ├── CrawlPlayer.java    # 캐릭터 기어가기 애니메이션
│   │   ├── Portal.java          # 포털 애니메이션
│   │   └── StarTexture.java     # 별 애니메이션
│   └── build.gradle
├── lwjgl3/                      # 데스크톱 실행 및 패키징
│   ├── src/main/java/io/jbnu/hw1/lwjgl3/
│   │   ├── Lwjgl3Launcher.java
│   │   └── StartupHelper.java
│   └── build.gradle
├── gradle/                      # Gradle Wrapper 및 JVM 설정
├── build.gradle
├── settings.gradle
├── gradle.properties
├── gradlew
└── gradlew.bat
```


이 프로젝트는 [libGDX](https://libgdx.com/)와 [gdx-liftoff](https://github.com/libgdx/gdx-liftoff)로 생성한 프로젝트 구조를 기반으로 합니다.
