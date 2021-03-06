/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

internal operator fun <T> Provider<T>.getValue(thisRef: Any?, property: KProperty<*>) = get()

internal operator fun <T> Property<T>.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    set(value)
}

private class OptionalProviderDelegate<T>(private val provider: Provider<T?>) : ReadOnlyProperty<Any?, T?> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T? =
        if (provider.isPresent)
            provider.get()
        else null
}

internal fun <T> Project.optionalProvider(initialize: () -> T?): ReadOnlyProperty<Any?, T?> =
    OptionalProviderDelegate(provider(initialize))

internal fun <T : Any> Project.newProperty(initialize: (() -> T)? = null): Property<T> =
    @Suppress("UNCHECKED_CAST")
    (project.objects.property(Any::class.java) as Property<T>).apply {
        if (initialize != null)
            set(provider(initialize))
    }

// Before 5.0 fileProperty is created via ProjectLayout
// https://docs.gradle.org/current/javadoc/org/gradle/api/model/ObjectFactory.html#fileProperty--
internal fun Project.newFileProperty(initialize: (() -> File)? = null): RegularFileProperty {
    val regularFileProperty = project.objects.fileProperty()

    return regularFileProperty.apply {
        if (initialize != null) {
            set(project.layout.file(project.provider(initialize)))
        }
    }
}