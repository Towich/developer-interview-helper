#!/usr/bin/env python3
"""Генерирует JSON-темы для AchLine из структуры папок android-interview (Android/Kotlin/Gradle)."""
from __future__ import annotations

import json
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1] / "composeApp/src/commonMain/composeResources/files/interview"


def bundle(tech: str, cat: str, theme_id: str, title: str, prefix: str, qa: list[tuple[str, str, str]]) -> dict:
    assert len(qa) == 5
    diffs = ["easy", "medium", "medium", "hard", "hard"]
    return {
        "schemaVersion": 1,
        "technologyId": tech,
        "categoryId": cat,
        "themeId": theme_id,
        "themeTitle": title,
        "questions": [
            {
                "id": f"{prefix}-{i + 1:03d}",
                "questionText": q,
                "answerText": a,
                "difficulty": diffs[i],
            }
            for i, (q, a, _) in enumerate(qa)
        ],
    }


def write(rel: str, data: dict) -> None:
    p = ROOT / rel
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


# Кортежи вопросов: только (question, answer) — сложность задаётся порядком easy,med,med,hard,hard
def qa(*pairs: tuple[str, str]) -> list[tuple[str, str, str]]:
    return [(q, a, "") for q, a in pairs]


THEMES: list[tuple[str, dict]] = []

# ——— Android (подпапки) ———
THEMES += [
    (
        "android/compose/jetpack_compose.json",
        bundle(
            "android",
            "compose",
            "jetpack_compose",
            "Jetpack Compose",
            "android-compose-jpc",
            qa(
                (
                    "Что такое recomposition в Jetpack Compose?",
                    "Повторный вызов @Composable при изменении отслеживаемого состояния; Compose обновляет только затронутые участки дерева.",
                ),
                (
                    "Зачем remember { mutableStateOf(...) } вместо одного mutableStateOf?",
                    "remember фиксирует один экземпляр state в узле композиции; без remember значение сбрасывалось бы при каждой рекомпозиции.",
                ),
                (
                    "Чем отличаются LaunchedEffect(Unit) и LaunchedEffect(key)?",
                    "Unit запускает блок один раз при входе в композицию (пока не вышли). С ключом блок отменяется и перезапускается при смене key.",
                ),
                (
                    "Как работает snapshot system Compose и связь с recomposition?",
                    "mutableState отслеживается snapshot-ом; при записи помечаются затронутые читатели, планируется рекомпозиция наблюдателей.",
                ),
                (
                    "Когда нужен SubcomposeLayout и какие компромиссы?",
                    "Когда измерение дочерних элементов зависит от доступного места/друг друга (например, сложные адаптивные макеты). Сложнее отладка и стоимость измерения.",
                ),
            ),
        ),
    ),
    (
        "android/view/view_xml.json",
        bundle(
            "android",
            "view",
            "view_xml",
            "View и XML",
            "android-view-xml",
            qa(
                (
                    "Что такое View в Android?",
                    "Базовый UI-элемент на экране: занимает прямоугольник, рисуется в onDraw, обрабатывает события и измеряется/раскладывается layout-ом.",
                ),
                (
                    "Чем отличаются match_parent и wrap_content?",
                    "match_parent — занять всё доступное в родителе по оси; wrap_content — размер по содержимому согласно onMeasure.",
                ),
                (
                    "Для чего нужен ViewStub?",
                    "Ленивое раздувание части иерархии: пока не inflated, не создаёт дочерние View — экономия на старте сложных экранов.",
                ),
                (
                    "Как устроены measure/layout passes у ViewGroup?",
                    "Два прохода: measure (определение размеров с учётом режимов AT_MOST/EXACTLY/UNSPECIFIED), затем layout (позиции детей). Нарушение контрактов ведёт к лишним проходам.",
                ),
                (
                    "Почему нельзя держать ссылки на View после destroy Activity без осторожности?",
                    "Риск утечек через контекст/слушатели; после onDestroy нужно снимать listeners, отменять анимации, не хранить Activity в static.",
                ),
            ),
        ),
    ),
    (
        "android/architecture/ui_data.json",
        bundle(
            "android",
            "architecture",
            "ui_data",
            "Архитектура UI и данные",
            "android-arch-ui",
            qa(
                (
                    "Что такое MVVM на Android?",
                    "Разделение: View (Activity/Fragment/Compose), ViewModel хранит UI-состояние и бизнес-вызовы, Model — данные/репозитории.",
                ),
                (
                    "Зачем Repository между ViewModel и источниками данных?",
                    "Единая точка доступа к сети/БД/кэшу, кэширование, маппинг DTO→domain, тестируемость ViewModel.",
                ),
                (
                    "Чем StateFlow отличается от SharedFlow для UI-состояния?",
                    "StateFlow всегда хранит последнее значение (удобно для экрана). SharedFlow — события без обязательного последнего значения (навигация, снэкбары), нужен replay/костыли для состояния.",
                ),
                (
                    "Как Single Activity + Navigation Component влияет на жизненный цикл ViewModel?",
                    "NavBackStackEntry как владелец ViewModel по графу; важно задавать правильный scope и не смешивать глобальные синглтоны с экранными VM.",
                ),
                (
                    "Какие проблемы даёт «бог-Activity» и как их снять?",
                    "Смешение UI, навигации, сети и БД; вынос use-case-ов, модульность, UDF/MVI, тесты на domain без Robolectric.",
                ),
            ),
        ),
    ),
    (
        "android/security/privacy.json",
        bundle(
            "android",
            "security",
            "privacy",
            "Безопасность и приватность",
            "android-sec",
            qa(
                (
                    "Зачем использовать HTTPS вместо HTTP в приложении?",
                    "Шифрование канала, защита от подслушивания и MITM (при корректной проверке сертификатов и pinning при необходимости).",
                ),
                (
                    "Где безопаснее хранить токены: SharedPreferences или EncryptedSharedPreferences?",
                    "EncryptedSharedPreferences (или Keystore-обёртки) — данные шифруются; обычные prefs в root/бэкапах уязвимы.",
                ),
                (
                    "Что такое Android Keystore?",
                    "Хранилище ключей в TEE/StrongBox; ключи не извлекаются в открытом виде, операции подписи/шифрования выполняются на устройстве.",
                ),
                (
                    "Какие риски у exported=true у компонентов?",
                    "Внешние приложения могут вызывать Activity/Service/Receiver; нужны разрешения, проверка caller, фильтры intent.",
                ),
                (
                    "Как правильно работать с персональными данными и политикой приватности?",
                    "Минимизация сбора, явное согласие, политика в store, Data Safety form, удаление по запросу, ограничение логов.",
                ),
            ),
        ),
    ),
    (
        "android/extras/misc.json",
        bundle(
            "android",
            "extras",
            "misc",
            "Дополнительно (Android)",
            "android-x",
            qa(
                (
                    "Что такое App Shortcuts?",
                    "Ярлыки долгого нажатия на иконку: статические в манифесте/ресурсах и динамические через ShortcutManager.",
                ),
                (
                    "Зачем нужны WindowInsets?",
                    "Учёт вырезов, статус-бара, клавиатуры, жестовой навигации при отступах контента.",
                ),
                (
                    "Что такое Picture-in-Picture?",
                    "Режим окна поверх других приложений для видео/созвонов; требует поддержки Activity и пользовательских разрешений.",
                ),
                (
                    "Как работает predictive back gesture?",
                    "Предпросмотр назначения при жесте «назад»; интеграция с OnBackPressedDispatcher/Navigation и анимациями.",
                ),
                (
                    "Какие особенности больших экранов (foldables, tablets)?",
                    "Адаптивные макеты, resizable activities, multi-window, сохранение состояния при смене конфигурации и соотношения сторон.",
                ),
            ),
        ),
    ),
    (
        "android/platform/components.json",
        bundle(
            "android",
            "platform",
            "components",
            "Платформа и компоненты",
            "android-plat",
            qa(
                (
                    "Что такое Activity?",
                    "Компонент UI с окном; жизненный цикл onCreate…onDestroy; точка входа для экрана и intent-фильтров.",
                ),
                (
                    "Чем Fragment отличается от Activity?",
                    "Фрагмент — контролируемый кусок UI внутри Activity, свой жизненный цикл, back stack через FragmentManager.",
                ),
                (
                    "Для чего нужен Service?",
                    "Фоновая работа без UI; foreground service с уведомлением для долгих задач; ограничения на фон с Android 8+.",
                ),
                (
                    "Как доставляются BroadcastReceiver и чем ordered broadcast отличается?",
                    "Система рассылает intent подписчикам; ordered — по цепочке с возможностью abort; implicit broadcast ограничены для фона.",
                ),
                (
                    "Как работает процесс и task affinity для Activity?",
                    "Affinity группирует задачи; launchMode/singleTask влияет на стек; важно для deeplink и мультиоконности.",
                ),
            ),
        ),
    ),
    (
        "android/performance/threads_memory.json",
        bundle(
            "android",
            "performance",
            "threads_memory",
            "Потоки, производительность и память",
            "android-perf",
            qa(
                (
                    "Почему нельзя блокировать main thread?",
                    "ANR при долгих операциях; UI не обрабатывает события ~5 c; сеть/БД — в фоне.",
                ),
                (
                    "Что такое ANR и как его диагностировать?",
                    "Application Not Responding; traces в logcat/Play Vitals; профилировщик, StrictMode, main-thread watchdog.",
                ),
                (
                    "Как бороться с утечками памяти из-за Handler?",
                    "Использовать static Runnable + WeakReference, убирать callbacks в onDestroy, lifecycle-aware корутины.",
                ),
                (
                    "Чем профилировать jank и overdraw?",
                    "GPU rendering profile, Layout Inspector, Systrace/Perfetto; уменьшение вложенности, кэширование measure, hardware layers осторожно.",
                ),
                (
                    "Как работает GC в ART на уровне интервью?",
                    "Поколенческий сборщик, pause times, большие объекты; избегать лишних аллокаций в горячих путях UI.",
                ),
            ),
        ),
    ),
    (
        "android/build/release.json",
        bundle(
            "android",
            "build",
            "release",
            "Сборка и релиз",
            "android-build",
            qa(
                (
                    "Чем debug-сборка отличается от release?",
                    "Release: minify/shrinker, подпись, оптимизации, часто отключённый debuggable; другие константы BuildConfig.",
                ),
                (
                    "Зачем R8/ProGuard?",
                    "Сжатие, обфускация, удаление неиспользуемого кода; нужны keep-правила для рефлексии.",
                ),
                (
                    "Что такое App Bundle и dynamic delivery?",
                    "AAB в Play; сплиты по ABI/языку; Play Feature Delivery для модульных фич.",
                ),
                (
                    "Как устроена подпись приложения Play App Signing?",
                    "Google держит ключ подписи дистрибуции; upload key для разработчика; восстановление при потере ключа.",
                ),
                (
                    "Какие проверки перед продакшеном (security checklist)?",
                    "Отключить логи PII, проверить network security config, экспорт компонентов, root detection при необходимости, лицензии зависимостей.",
                ),
            ),
        ),
    ),
    (
        "android/network/serialization.json",
        bundle(
            "android",
            "network",
            "serialization",
            "Сеть и сериализация",
            "android-net",
            qa(
                (
                    "Что такое Retrofit?",
                    "Декларативный HTTP-клиент на OkHttp: интерфейсы с аннотациями, конвертеры (Moshi/Gson), suspend/call adapters.",
                ),
                (
                    "Зачем interceptor в OkHttp?",
                    "Перехват цепочки запросов: логирование, заголовки, токены, retry, кэш.",
                ),
                (
                    "Чем JSON отличается от Protocol Buffers в мобильном клиенте?",
                    "JSON текстовый и удобен для отладки; protobuf компактнее и быстрее, но нужна схема и генерация кода.",
                ),
                (
                    "Как обрабатывать медленную сеть и таймауты?",
                    "Разумные timeouts, retry с backoff, offline-кэш, idempotency для мутаций, UX с состоянием загрузки.",
                ),
                (
                    "Что такое certificate pinning и когда он нужен?",
                    "Фиксация доверенных ключей/пинов для защиты от поддельных CA в корпоративных/высокорисковых сценариях; усложняет ротацию.",
                ),
            ),
        ),
    ),
    (
        "android/testing/basics.json",
        bundle(
            "android",
            "testing",
            "basics",
            "Тестирование",
            "android-test",
            qa(
                (
                    "Чем unit-тест отличается от instrumented-теста?",
                    "Unit на JVM без устройства; instrumented на эмуляторе/девайсе с Android framework.",
                ),
                (
                    "Что такое Espresso?",
                    "Фреймворк UI-тестов: синхронизация с main thread, matchers, idling resources.",
                ),
                (
                    "Зачем mock в тестах репозитория?",
                    "Изолировать ViewModel/use-case от сети/БД; предсказуемые сценарии и быстрые тесты.",
                ),
                (
                    "Как тестировать Compose?",
                    "composeTestRule, семантика и testTag, idle для анимаций; избегать хрупких селекторов по тексту где возможно.",
                ),
                (
                    "Что такое флайки-тесты и как снизить flakiness?",
                    "Нестабильные из-за таймингов/анимаций; явные ожидания, стабильные id, отключение анимаций в CI, повтор с диагностикой.",
                ),
            ),
        ),
    ),
    (
        "android/background/services.json",
        bundle(
            "android",
            "background",
            "services",
            "Фоновая работа и системные сервисы",
            "android-bg",
            qa(
                (
                    "Что такое WorkManager?",
                    "Отложенная гарантированная фоновая работа с учётом Doze, constraints (сеть, зарядка), цепочки задач.",
                ),
                (
                    "Чем JobScheduler отличается от AlarmManager?",
                    "JobScheduler — батчинг и условия ОС; AlarmManager — точные/повторяющиеся будильники, ограничения на точность во фоне.",
                ),
                (
                    "Зачем foreground service?",
                    "Долгие задачи, заметные пользователю (трекинг, плеер); обязательное уведомление; типы FGS с Android 14+.",
                ),
                (
                    "Как Doze и App Standby влияют на фон?",
                    "Откладывают сеть и wake locks; нужны правильные API (WorkManager, FGS) вместо вечных сервисов.",
                ),
                (
                    "Когда уместен BroadcastReceiver vs WorkManager?",
                    "Receiver — для короткой реакции на системное событие; тяжёлую работу переносить в WorkManager/FGS.",
                ),
            ),
        ),
    ),
    (
        "android/storage/persistence.json",
        bundle(
            "android",
            "storage",
            "persistence",
            "Хранение данных",
            "android-store",
            qa(
                (
                    "Что такое SharedPreferences?",
                    "Key-value XML на диске; для мелких настроек; не для больших данных; async apply vs commit.",
                ),
                (
                    "Когда выбрать Datastore вместо SharedPreferences?",
                    "Типобезопасность, корутины/Flow, меньше ANR на больших записях, миграция с prefs.",
                ),
                (
                    "Чем Room отличается от сырого SQLite?",
                    "ORM-слой, compile-time проверка запросов, миграции, связь с Flow/LiveData.",
                ),
                (
                    "Как версионировать схему БД в Room?",
                    "@Database version, Migration объектами, destructive fallback только для dev.",
                ),
                (
                    "Какие нюансы кэширования изображений (Coil/Glide)?",
                    "Память/диск, размеры, placeholder, очистка при нехватке памяти, корректная отмена запросов в списках.",
                ),
            ),
        ),
    ),
]

# ——— Kotlin ———
THEMES += [
    (
        "kotlin/concurrency/coroutines_flow.json",
        bundle(
            "kotlin",
            "concurrency",
            "coroutines_flow",
            "Coroutines и Flow",
            "kotlin-co",
            qa(
                (
                    "Чем корутина отличается от потока ОС?",
                    "Корутина легковесна и планируется в userspace на ограниченном пуле потоков; поток ОС дороже по памяти и переключению.",
                ),
                (
                    "Для чего нужен suspend?",
                    "Помечает функцию с точками приостановки без блокировки потока; continuation-преобразование компилятором.",
                ),
                (
                    "Что такое structured concurrency?",
                    "Иерархия Job: отмена детей с родителем, supervisor для изолированных ошибок, scope задаёт границу жизни.",
                ),
                (
                    "Cold vs hot Flow — в чём разница?",
                    "Cold эмитит заново каждому подписчику; hot (SharedState/Channel) — мультикаст и разделяемое состояние.",
                ),
                (
                    "Как избежать отмены долгой операции в finally без NonCancellable?",
                    "Делить работу на чекпоинты, withContext(NonCancellable) только для критичных cleanup-участков, корректные try/finally.",
                ),
            ),
        ),
    ),
    (
        "kotlin/language/null_safety.json",
        bundle(
            "kotlin",
            "language",
            "null_safety",
            "Null safety и типы",
            "kotlin-null",
            qa(
                (
                    "Чем T? отличается от T в Kotlin?",
                    "T? допускает null; T — non-null тип; компилятор отслеживает проверки и smart cast.",
                ),
                (
                    "Что делает оператор ?: (Elvis)?",
                    "Возвращает правую часть, если слева null; удобно для значений по умолчанию.",
                ),
                (
                    "Чем отличаются !! и requireNotNull?",
                    "!! бросает NPE с малоинформативным сообщением; requireNotNull даёт явное сообщение и используется для контрактов.",
                ),
                (
                    "Как работает платформенный тип из Java?",
                    "Неизвестная nullability; ответственность на разработчике; лучше аннотировать @Nullable/@NonNull на Java-стороне.",
                ),
                (
                    "Что такое sealed class и зачем в моделировании состояний?",
                    "Закрытый набор подтипов — исчерпывающий when без else, безопасные state-машины для UI.",
                ),
            ),
        ),
    ),
    (
        "kotlin/reactive/rxjava.json",
        bundle(
            "kotlin",
            "reactive",
            "rxjava",
            "RxJava для собеседований",
            "kotlin-rx",
            qa(
                (
                    "Что такое Observable в RxJava?",
                    "Поток событий 0..N элементов; подписчик получает onNext/onError/onComplete.",
                ),
                (
                    "Чем Single отличается от Completable?",
                    "Single — один элемент или ошибка; Completable — только завершение/ошибка без значения.",
                ),
                (
                    "Зачем subscribeOn и observeOn?",
                    "subscribeOn — где стартует цепочка (часто IO); observeOn — где получают события (main для UI).",
                ),
                (
                    "Как избежать утечек в Rx на Android?",
                    "CompositeDisposable, dispose в onDestroy, автоматизация через RxLifecycle/autodispose.",
                ),
                (
                    "В чём разница между map и flatMap?",
                    "map преобразует элемент; flatMap разворачивает вложенный Observable и сливает потоки (merge/switch).",
                ),
            ),
        ),
    ),
    (
        "kotlin/interop/java.json",
        bundle(
            "kotlin",
            "interop",
            "java",
            "Взаимодействие с Java",
            "kotlin-java",
            qa(
                (
                    "Как вызвать Kotlin default-параметры из Java?",
                    "Через @JvmOverloads на конструкторах/функциях или сгенерированные перегрузки.",
                ),
                (
                    "Что такое @JvmStatic?",
                    "Статический метод на классе Java для функции/проперти в companion object.",
                ),
                (
                    "Как Kotlin name mangling решает конфликты с Java?",
                    "Сгенерированные имена для функций с default-параметрами; @JvmName для явного имени.",
                ),
                (
                    "Почему MutableList из Kotlin может удивить Java-код?",
                    "Список может быть обёрткой с модификациями; для Java API иногда нужны явные типы и копии.",
                ),
                (
                    "Как nullable Kotlin стыкуется с Optional в Java API?",
                    "Использовать platform types осторожно; оборачивать/маппить; предпочитать чистые Kotlin API.",
                ),
            ),
        ),
    ),
    (
        "kotlin/language/delegation.json",
        bundle(
            "kotlin",
            "language",
            "delegation",
            "Делегирование и свойства",
            "kotlin-del",
            qa(
                (
                    "Что такое делегирование класса в Kotlin?",
                    "class Foo(bar: Bar) : Interface by bar — переадресация методов интерфейса делегату.",
                ),
                (
                    "Что делает lazy { } для свойства?",
                    "Инициализация при первом обращении; по умолчанию thread-safe (Synchronized).",
                ),
                (
                    "Как работает observable delegate (Delegates.observable)?",
                    "Колбэк после изменения значения; удобно для логирования/биндинга с осторожностью к рекурсии.",
                ),
                (
                    "Чем отличаются lateinit var от by lazy?",
                    "lateinit для ненуллабельных с поздней инициализацией без значения; lazy — вычисление значения при первом чтении.",
                ),
                (
                    "Когда использовать custom property delegate?",
                    "Повторяющаяся логика (prefs, savedStateHandle-обёртки), единообразный доступ к ресурсам.",
                ),
            ),
        ),
    ),
    (
        "kotlin/language/generics.json",
        bundle(
            "kotlin",
            "language",
            "generics",
            "Дженерики и вариантность",
            "kotlin-gen",
            qa(
                (
                    "Что такое дженерик-класс List<T>?",
                    "Параметризация типом; компилятор обеспечивает типобезопасность на этапе компиляции (type erasure на JVM).",
                ),
                (
                    "Чем отличаются in и out (вариантность)?",
                    "out — ковариантность (производитель), можно читать T; in — контравариантность (потребитель), можно писать T.",
                ),
                (
                    "Почему Array инвариантен в Kotlin?",
                    "Массивы мутируемы; ковариантность сломала бы типобезопасность при записи.",
                ),
                (
                    "Что такое star projection List<*>?",
                    "Неизвестный аргумент; для чтения — Any?; запись обычно запрещена кроме специальных случаев.",
                ),
                (
                    "Как reified помогает в inline-функциях?",
                    "Тип доступен во время компиляции в теле inline; можно использовать ::class без Class-токена.",
                ),
            ),
        ),
    ),
    (
        "kotlin/extras/misc.json",
        bundle(
            "kotlin",
            "extras",
            "misc",
            "Дополнительно (Kotlin)",
            "kotlin-x",
            qa(
                (
                    "Что такое data class?",
                    "Автоequals/hashCode/toString/copy; удобен для DTO и immutable моделей.",
                ),
                (
                    "Зачем object в Kotlin?",
                    "Синглтон или объект выражения; companion object — статическая область для класса.",
                ),
                (
                    "Что такое inline class (value class)?",
                    "Обёртка без аллокации в ряде случаев; типобезопасные идентификаторы без оверхеда класса-оболочки.",
                ),
                (
                    "Как работает contract в стандартной библиотеке?",
                    "Дополнительные гарантии для компилятора (например, isTrue -> value не null); осторожно с корректностью.",
                ),
                (
                    "Чем open class отличается от sealed/interface для дизайна API?",
                    "open — наследование по умолчанию разрешено; sealed — закрытый набор; interface — контракт без состояния (до 1.8 осторожно с полями).",
                ),
            ),
        ),
    ),
    (
        "kotlin/oop/classes.json",
        bundle(
            "kotlin",
            "oop",
            "classes",
            "Классы, ООП и модификаторы",
            "kotlin-oop",
            qa(
                (
                    "Чем final класс по умолчанию в Kotlin отличается от Java?",
                    "В Kotlin классы закрыты для наследования без open; в Java наоборот virtual по умолчанию.",
                ),
                (
                    "Что такое primary constructor?",
                    "Параметры класса и свойства в заголовке; init-блоки для дополнительной логики.",
                ),
                (
                    "Зачем internal модификатор?",
                    "Видимость в пределах модуля (Gradle module) — удобно для скрытия API внутри библиотеки.",
                ),
                (
                    "Как устроено наследование при наличии data и open?",
                    "data + наследование ограничено; copy/equals могут вести себя неожиданно — проектируют sealed иерархии.",
                ),
                (
                    "Что такое тип алиас typealias?",
                    "Синоним имени типа для читаемости; не создаёт новый тип на JVM (стирается до базового).",
                ),
            ),
        ),
    ),
    (
        "kotlin/collections/sequences.json",
        bundle(
            "kotlin",
            "collections",
            "sequences",
            "Коллекции и последовательности",
            "kotlin-coll",
            qa(
                (
                    "Чем List отличается от MutableList?",
                    "List — read-only интерфейс; MutableList позволяет изменять содержимое.",
                ),
                (
                    "Когда использовать Set вместо List?",
                    "Уникальность элементов и быстрые проверки contains для реализаций на хэшах.",
                ),
                (
                    "Чем Sequence отличается от Iterable для цепочек map/filter?",
                    "Sequence ленивая (промежуточные операции без немедленного списка); Iterable часто строит промежуточные коллекции.",
                ),
                (
                    "Какая сложность у get для LinkedHashMap vs HashMap?",
                    "Обычно O(1) среднее для обеих; LinkedHashMap хранит порядок вставки с небольшим оверхедом памяти.",
                ),
                (
                    "Почему опасно модифировать коллекцию во время итерации?",
                    "ConcurrentModificationException; использовать iterator.remove, snapshot-копии или структуры для concurrent сценариев.",
                ),
            ),
        ),
    ),
    (
        "kotlin/advanced/advanced.json",
        bundle(
            "kotlin",
            "advanced",
            "advanced",
            "Продвинутые конструкции",
            "kotlin-adv",
            qa(
                (
                    "Что такое inline-функция?",
                    "Тело подставляется в место вызова; можно использовать reified, снижает стоимость лямбд при non-local returns осторожно.",
                ),
                (
                    "Что такое crossinline/noinline для лямбд?",
                    "noinline — не инлайнится; crossinline запрещает non-local return из лямбды переданной в другой контекст.",
                ),
                (
                    "Как работает DSL с @DslMarker?",
                    "Ограничивает неявные receivers, чтобы вложенные блоки не перепутали контексты.",
                ),
                (
                    "Что такое context receivers (Kotlin)?",
                    "Явные требования контекста к функции без передачи параметром; влияет на сигнатуры и читаемость API.",
                ),
                (
                    "Когда применять expect/actual в KMP?",
                    "Объявление в common, реализация per-platform; для IO, крипто, UI-примитивов.",
                ),
            ),
        ),
    ),
    (
        "kotlin/basics/syntax.json",
        bundle(
            "kotlin",
            "basics",
            "syntax",
            "Синтаксис и основы языка",
            "kotlin-base",
            qa(
                (
                    "Чем val отличается от var?",
                    "val — неизменяемая ссылка (read-only); var — можно переприсваивать.",
                ),
                (
                    "Что такое строковые шаблоны \"$name\"?",
                    "Интерполяция выражений в строках; для сложных выражений — фигурные скобки ${}.",
                ),
                (
                    "Как работает when как выражение?",
                    "Возвращает значение ветки; исчерпывающий для sealed/enum; else для неполных типов.",
                ),
                (
                    "Что такое smart cast после проверки типа?",
                    "Компилятор сужает тип после is/as? без явного каста в безопасном блоке.",
                ),
                (
                    "Чем break с меткой отличается от обычного break?",
                    "Выход из внешнего цикла/когда по @label; полезно для вложенных циклов.",
                ),
            ),
        ),
    ),
    (
        "kotlin/functions/lambdas.json",
        bundle(
            "kotlin",
            "functions",
            "lambdas",
            "Функции, лямбды и области видимости",
            "kotlin-fn",
            qa(
                (
                    "Что такое функция расширения?",
                    "Синтаксический сахар: fun T.foo() — первый параметр receiver типа T.",
                ),
                (
                    "Чем it отличается от явного имени в лямбде одного параметра?",
                    "it — неявное имя; явное имя улучшает читаемость в сложных лямбдах.",
                ),
                (
                    "Что делают scope-функции let/apply/run/with/also?",
                    "let/also передают объект как it/this и возвращают результат блока/сам объект; apply/run/with — конфигурация receiver.",
                ),
                (
                    "Что такое trailing lambda?",
                    "Лямбда вне скобок последним аргументом; синтаксис DSL и коллекций.",
                ),
                (
                    "Как работает локальная функция в Kotlin?",
                    "Функция внутри функции видит внешние переменные (замыкание); удобно для разбиения без публичного API.",
                ),
            ),
        ),
    ),
]

# ——— Gradle ———
THEMES += [
    (
        "gradle/lifecycle/basics.json",
        bundle(
            "gradle",
            "lifecycle",
            "basics",
            "Основы и жизненный цикл Gradle",
            "gradle-lc",
            qa(
                (
                    "Что такое Gradle?",
                    "Система сборки на JVM с моделью задач, DSL на Groovy/Kotlin, плагинами и кэшами.",
                ),
                (
                    "Чем configuration phase отличается от execution phase?",
                    "Configuration строит граф задач и настройки; execution выполняет действия задач.",
                ),
                (
                    "Зачем Gradle Daemon?",
                    "Долгоживущий JVM-процесс ускоряет инкрементальные сборки за счёт кэша классовloader и состояния.",
                ),
                (
                    "Что такое task inputs/outputs?",
                    "Декларация для инкрементальности и build cache; неверные inputs ведут к некорректному UP-TO-DATE.",
                ),
                (
                    "Как работает Gradle Wrapper?",
                    "gradlew фиксирует версию дистрибутива; воспроизводимые сборки в CI и у команды.",
                ),
            ),
        ),
    ),
    (
        "gradle/plugins/scripts.json",
        bundle(
            "gradle",
            "plugins",
            "scripts",
            "Плагины и скрипты",
            "gradle-plg",
            qa(
                (
                    "Чем plugins { } отличается от buildscript + apply?",
                    "plugins DSL управляет версиями через pluginManagement, проще и декларативнее для большинства случаев.",
                ),
                (
                    "Что такое convention plugin (precompiled script)?",
                    "Переиспользуемая конфигурация как отдельный модуль плагина; единый стиль для многомодульности.",
                ),
                (
                    "Когда уместен buildSrc?",
                    "Общий код/плагины внутри репозитория; альтернатива — included composite build.",
                ),
                (
                    "Как объявить кастомную Gradle Task?",
                    "Наследование DefaultTask, @TaskAction, регистрация tasks.register; избегать тяжёлой работы на configuration.",
                ),
                (
                    "Что такое configuration cache и его ограничения?",
                    "Сериализация конфигурации для ускорения; задачи/плагины должны быть совместимы (no mutable globals at config time).",
                ),
            ),
        ),
    ),
    (
        "gradle/dsl/projects.json",
        bundle(
            "gradle",
            "dsl",
            "projects",
            "DSL файлы и проекты",
            "gradle-dsl",
            qa(
                (
                    "Для чего settings.gradle.kts?",
                    "Имя проекта, include модулей, pluginManagement, dependencyResolutionManagement.",
                ),
                (
                    "Что задаёт build.gradle.kts у модуля?",
                    "Плагины, зависимости, android {} или kotlin {} блоки, flavors, signing для приложения.",
                ),
                (
                    "Зачем gradle.properties?",
                    "Флаги JVM, org.gradle.parallel, kotlin options, кастомные ключи для CI.",
                ),
                (
                    "Что такое extra properties ext / typed accessors?",
                    "Хранение переменных на проекте; в Kotlin DSL предпочтительны buildscript или extensions.",
                ),
                (
                    "Как связаны rootProject и subprojects?",
                    "Иерархия проектов; allprojects/subprojects для общих репозиториев и версий (осторожно с coupling).",
                ),
            ),
        ),
    ),
    (
        "gradle/modules/structure.json",
        bundle(
            "gradle",
            "modules",
            "structure",
            "Многомодульность и структура репозитория",
            "gradle-mod",
            qa(
                (
                    "Зачем разбивать проект на модули?",
                    "Изоляция фич, параллельная сборка, переиспользование, границы зависимостей.",
                ),
                (
                    "Чем api отличается от implementation в Gradle?",
                    "api протаскивает зависимость транзитивно наружу; implementation — внутренняя деталь модуля.",
                ),
                (
                    "Что такое composite build?",
                    "Подключение другого Gradle-проекта через includeBuild для локальной разработки библиотек.",
                ),
                (
                    "Как избежать циклических зависимостей между модулями?",
                    "Выделить :core:api и :core:impl, инверсия зависимостей, feature-модули зависят от интерфейсов.",
                ),
                (
                    "Зачем централизовать версии в root?",
                    "Единообразие, меньше дрейфа версий между модулями; Version Catalog усиливает это.",
                ),
            ),
        ),
    ),
    (
        "gradle/catalog/versions.json",
        bundle(
            "gradle",
            "catalog",
            "versions",
            "Version Catalog и управление версиями",
            "gradle-vcat",
            qa(
                (
                    "Что такое libs.versions.toml?",
                    "Файл каталога версий, библиотек и плагинов для type-safe accessors в Gradle Kotlin DSL.",
                ),
                (
                    "Чем bundle отличается от отдельных library aliases?",
                    "bundle группирует набор зависимостей одной строкой в dependencies блоке.",
                ),
                (
                    "Как объявить плагин в catalog?",
                    "Секция [plugins] + id и version; в root plugins { alias(libs.plugins.xxx) }.",
                ),
                (
                    "Зачем dependencyResolutionManagement в settings?",
                    "Принудительные репозитории, BOM/platform, единые правила для всех subprojects.",
                ),
                (
                    "Как мигрировать с ext версий на catalog поэтапно?",
                    "Начать с новых модулей, постепенно заменять строки, проверить IDE подсветку и CI кэш.",
                ),
            ),
        ),
    ),
    (
        "gradle/dependencies/resolution.json",
        bundle(
            "gradle",
            "dependencies",
            "resolution",
            "Зависимости и разрешение артефактов",
            "gradle-dep",
            qa(
                (
                    "Что такое Maven Central и google() репозитории?",
                    "Источники артефактов; google() для AndroidX/AGP, Maven Central для большинства OSS.",
                ),
                (
                    "Чем implementation отличается от compileOnly?",
                    "compileOnly доступна только на compile classpath (например аннотации); не попадает в runtime apk.",
                ),
                (
                    "Что такое BOM (Bill of Materials)?",
                    "platform(...) фиксирует согласованные версии набора библиотек (например Firebase BOM).",
                ),
                (
                    "Как разрешить конфликт версий транзитивных зависимостей?",
                    "resolutionStrategy.force, constraints, exclude с обоснованием, dependencyInsight задача.",
                ),
                (
                    "Что делают dependency substitution rules?",
                    "Заменить модуль на project() или другую версию для composite/local разработки.",
                ),
            ),
        ),
    ),
    (
        "gradle/agp/android_link.json",
        bundle(
            "gradle",
            "agp",
            "android_link",
            "Android Gradle Plugin и связка с Android",
            "gradle-agp",
            qa(
                (
                    "Зачем нужны compileSdk и targetSdk?",
                    "compileSdk — против какой платформы компилируем API; targetSdk — заявленное поведение совместимости для магазина.",
                ),
                (
                    "Чем application отличается от library плагина?",
                    "application собирает APK/AAB; library — AAR без финального манифеста приложения.",
                ),
                (
                    "Что такое buildTypes debug/release?",
                    "Разные настройки minify, signingConfig, buildConfigFields, proguard files.",
                ),
                (
                    "Как связаны Gradle tasks и AGP (assembleDebug)?",
                    "AGP регистрирует задачи вариантов; зависимости между mergeResources, dex, package и т.д.",
                ),
                (
                    "Зачем namespace в AGP 8+?",
                    "Замена package в AndroidManifest для R и BuildConfig; явная декларация пакета ресурсов.",
                ),
            ),
        ),
    ),
    (
        "gradle/performance/build_perf.json",
        bundle(
            "gradle",
            "performance",
            "build_perf",
            "Производительность сборки",
            "gradle-perf",
            qa(
                (
                    "Что даёт org.gradle.parallel=true?",
                    "Параллельное выполнение независимых модулей на многоядерных машинах.",
                ),
                (
                    "Зачем build cache?",
                    "Переиспользование выходов задач между сборками и CI; требует корректных inputs/outputs.",
                ),
                (
                    "Как уменьшить работу на configuration phase?",
                    "Избегать тяжёлых вычислений вне tasks, ленивая регистрация, configuration cache.",
                ),
                (
                    "Что такое remote build cache?",
                    "Общий кэш команды/CI; настройка URL и credentials; внимание к безопасности артефактов.",
                ),
                (
                    "Почему KAPT может замедлять сборку?",
                    "Генерация стабов, аннотационные процессоры; альтернатива KSP где возможно.",
                ),
            ),
        ),
    ),
    (
        "gradle/extras/misc.json",
        bundle(
            "gradle",
            "extras",
            "misc",
            "Дополнительно (Gradle)",
            "gradle-x",
            qa(
                (
                    "Зачем lint в Android-проекте?",
                    "Статический анализ ресурсов и кода; находит проблемы совместимости и перформанса.",
                ),
                (
                    "Где хранить signing secrets в CI?",
                    "Encrypted secrets store, environment variables, не коммитить keystore пароли в git.",
                ),
                (
                    "Что такое Gradle toolchain?",
                    "Выбор конкретной JDK для компиляции независимо от JVM на машине.",
                ),
                (
                    "Как Gradle взаимодействует с Kotlin compiler daemon?",
                    "Отдельный процесс для инкрементальной компиляции Kotlin; настройки в kotlinOptions.",
                ),
                (
                    "Зачем dependency verification (checksums)?",
                    "Защита от подмены артефактов в репозитории; gradle/verification-metadata.xml.",
                ),
            ),
        ),
    ),
]


def main() -> None:
    paths: list[str] = []
    for rel, data in THEMES:
        write(rel, data)
        paths.append(rel)
    paths.sort()
    index = {"schemaVersion": 1, "themePaths": paths}
    write("index.json", index)
    print(f"Wrote {len(paths)} themes + index.json under {ROOT}")


if __name__ == "__main__":
    main()
