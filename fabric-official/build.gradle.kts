import versioning.BuildConfig

val minecraft_version: String by project
val fabric_version: String by project

// Plugin choice rationale:
//   This module uses the short `fabric-loom` plugin (LoomGradlePlugin, the remap
//   variant) with `mappings(intermediary:0.0.0:v2)` — a published empty intermediary
//   stub. Because the stub has zero entries, the named→intermediary remap pass is
//   effectively a no-op, leaving Mojang-named bytecode untouched in remapJar output.
//   This is intentional and matches the practical effect of LoomNoRemap
//   (LoomNoRemapGradlePlugin via the fully-qualified `net.fabricmc.fabric-loom` id)
//   without requiring the different jar/task/configuration plumbing that PE's
//   fabric-official uses. See PE's fabric-official build.gradle.kts for the
//   alternative pattern. Both produce equivalent jars when source contains no
//   intermediary refs, which is the case here (and will remain the case when real
//   26.X-mojmap anticheat code lands — see KNOWN BLOCKERS comment below).
plugins {
    `maven-publish`
    alias(libs.plugins.fabric.loom)
    grim.`base-conventions`
    grim.`jij-conventions`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    // 26.X anticheat port — concrete remaining work (audited via attempted
    // source copy of fabric-intermediary, see commit history for the revert).
    // Compile errors after pulling :common + compileOnly stubs fell into:
    //
    //   A. Intermediary types in cloud-fabric + fabric-permissions-api public
    //      signatures (`Permissions.check(class_2168, String)`, etc.). javac
    //      can't resolve `class_NNNN` against Mojang-named MC, so any source
    //      file importing those APIs fails to compile. Fix: delete
    //      FabricPermissionRegistrationManager.java, FabricSenderFactory.java,
    //      command/FabricPlayerSelectorParser.java, manager/FabricParserDescriptorFactory.java
    //      and stub the loader plugin's getters to no-op equivalents. Grim's
    //      existing catch path (CloudHelper.create → NCDFE → silent fallback)
    //      lets the engine run without these.
    //
    //   B. fabric-api event modules ship intermediary-named bytecode that
    //      doesn't link against 26.X Mojang names. ServerLifecycleEvents.SERVER_STARTING
    //      and ServerTickEvents.END_SERVER_TICK callers need to be replaced
    //      with direct mixins into MinecraftServer.runServer() and
    //      MinecraftServer.tickServer(). Affects GrimACFabricEntryPoint.java,
    //      initables/FabricTickEndEvent.java, scheduler/*.
    //
    //   C. MC API drift 1.21.11 → 26.1.2. Sample symbols javac couldn't resolve:
    //      Entity.level (private — needs accessWidener applied at build),
    //      Player.inventory (private — accessWidener), CommandSourceStack.source
    //      (private — accessWidener), MinecraftServer.playerDataStorage
    //      (protected — accessWidener), PlayerDataStorage.playerDir (private —
    //      accessWidener), plus method renames in AbstractFabricPlatformServer
    //      (lines 13/17/39), FabricPlatformPlayerFactory (lines 113-114),
    //      AbstractFabricPlatformInventory (lines 20+ chains). Each needs the
    //      26.1.2-mojmap call updated.
    //
    //   D. The mc261 submodule needs concrete Fabric261PlatformServer +
    //      Fabric261PlatformPlayer + Fabric261LoaderPlugin analogous to mc12111
    //      in fabric-intermediary.
    //
    //   E. Build/remap packaging: prove access-widener application,
    //      mixin refmap generation, and nested-jar wiring all work under the
    //      empty intermediary:0.0.0:v2 stub before grinding through per-file
    //      API fixes. :common's existing AW assumptions may not apply cleanly
    //      to the no-op-remap pass — needs a smoke build before the real port.
    //
    // Order of operations (per codex r5 review): D first (write minimal mc261
    // platform/loader so compile targets exist) → E (verify the build pipeline
    // mechanically) → A (strip cloud/perms surface) → B (mixin-driven events
    // replacing fabric-api) → C (grind through Mojmap API drift last, once the
    // architecture is proven). Estimated 6-8h supervised.
    mappings("net.fabricmc:intermediary:0.0.0:v2")
    modImplementation(libs.fabric.loader)

    compileOnly(libs.packetevents.api)
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("org.apache.logging.log4j:log4j-api:2.24.3")
}

allprojects {
    apply(plugin = "fabric-loom")
    apply(plugin = "grim.base-conventions")
    apply(plugin = "maven-publish")

    repositories {
        if (BuildConfig.mavenLocalOverride) mavenLocal()

        exclusive("https://maven.fabricmc.net/") {
            includeGroup("net.fabricmc")
            includeGroup("net.fabricmc.fabric-api")
        }

        exclusive("https://repo.grim.ac/snapshots") {
            includeGroup("ac.grim.grimac")
            includeGroup("com.github.retrooper")
        }

        exclusive("https://jitpack.io", { mavenContent { releasesOnly() } }) {
            includeGroup("com.github.Fallen-Breath.conditional-mixin")
        }

        exclusive("https://repo.viaversion.com", { mavenContent { releasesOnly() } }) {
            includeGroup("com.viaversion")
        }

        exclusive("https://nexus.scarsz.me/content/repositories/releases", { mavenContent { releasesOnly() } }) {
            includeGroup("github.scarsz")
        }

        exclusive("https://repo.opencollab.dev/maven-releases/", { mavenContent { releasesOnly() } }) {
            includeGroup("org.geysermc.api")
        }

        exclusive("https://repo.opencollab.dev/maven-snapshots/", { mavenContent { snapshotsOnly() } }) {
            includeGroup("org.geysermc.floodgate")
            includeGroup("org.geysermc.cumulus")
            includeModule("org.geysermc", "common")
            includeModule("org.geysermc", "geyser-parent")
        }

        mavenCentral()
    }

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(25))
        }
    }

    dependencies {
        val libsx = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
        modImplementation(libsx.findLibrary("fabric-loader").get())
        // :common is intentionally NOT pulled here; its transitive PE dep would force
        // Loom to remap an intermediary-namespaced access widener against 0.0.0 (fails).
        // When real 26.X mappings land, re-add `implementation(project(":common"))`.
    }

    publishing.publications.create<MavenPublication>("maven") {
        artifact(tasks["remapJar"])
    }

    tasks {
        // Intermediary 0.0.0 has no "named" namespace, so source remap fails. Disable
        // sources jar generation in any subproject that registers it.
        matching { it.name == "remapSourcesJar" || it.name == "sourcesJar" }
            .configureEach { enabled = false }

        remapJar {
            archiveBaseName = if (project == project(":fabric-official")) {
                "${rootProject.name}-fabric-official"
            } else {
                "${rootProject.name}-fabric-${project.name}"
            }
            archiveVersion = rootProject.version as String
        }
    }
}

subprojects {
    dependencies {
        implementation(project(":fabric-official", configuration = "namedElements"))
        val libsx = rootProject.extensions.getByType<VersionCatalogsExtension>().named("libs")
        compileOnly(libsx.findLibrary("packetevents-api").get())
    }
}

subprojects.forEach {
    tasks.named("remapJar").configure {
        dependsOn("${it.path}:remapJar")
    }
}

tasks.remapJar.configure {
    subprojects.forEach { subproject ->
        subproject.tasks.matching { it.name == "remapJar" }.configureEach {
            nestedJars.from(this)
        }
    }
}
