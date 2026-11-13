package com.arzobispado.redpatrimonial.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// ----------------------------------------------------------------------
// ROOM = base de datos local del teléfono. Tiene 3 piezas:
//   1. Entity  -> describe una TABLA (una clase = una tabla).
//   2. DAO     -> describe las CONSULTAS que se pueden hacer a la tabla.
//   3. Database-> junta las tablas y entrega el DAO a la app.
// Room genera el código de acceso automáticamente (con KSP en tiempo de compilación).
// ----------------------------------------------------------------------

/**
 * Tabla "favoritos": cada fila es un templo o espacio que el usuario marcó con el corazón.
 * @PrimaryKey indica la columna que identifica cada fila (aquí, el id del nodo).
 */
@Entity(tableName = "favoritos")
data class FavoritoEntity(
    @PrimaryKey val idNodo: Long
)

/** Consultas de la tabla de favoritos. Room implementa las funciones a partir de las anotaciones. */
@Dao
interface FavoritoDao {
    /**
     * Devuelve los ids guardados. Al ser un Flow, la pantalla se actualiza sola
     * cada vez que cambia la tabla (no hace falta volver a pedir los datos).
     */
    @Query("SELECT idNodo FROM favoritos")
    fun observarIds(): Flow<List<Long>>

    /** Consulta si un nodo ya está guardado. "suspend" = se ejecuta en una corrutina, fuera del hilo principal. */
    @Query("SELECT EXISTS(SELECT 1 FROM favoritos WHERE idNodo = :idNodo)")
    suspend fun existe(idNodo: Long): Boolean

    /** Inserta el favorito. REPLACE evita error si ya existía. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(favorito: FavoritoEntity)

    /** Borra el favorito de ese nodo. */
    @Query("DELETE FROM favoritos WHERE idNodo = :idNodo")
    suspend fun borrar(idNodo: Long)
}

/**
 * Base de datos local de la app. Crea el archivo "red_patrimonial.db" en el teléfono.
 * version = 1: si cambian las tablas en el futuro, hay que subir este número.
 * exportSchema = false: no se guarda el esquema en un archivo aparte (no lo necesitamos para la prueba).
 */
@Database(entities = [FavoritoEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /** Entrega el DAO para hacer consultas. */
    abstract fun favoritoDao(): FavoritoDao

    companion object {
        /** Construye la base. Se llama una sola vez desde AppContainer.iniciar(). */
        fun crear(contexto: Context): AppDatabase =
            Room.databaseBuilder(contexto, AppDatabase::class.java, "red_patrimonial.db").build()
    }
}
