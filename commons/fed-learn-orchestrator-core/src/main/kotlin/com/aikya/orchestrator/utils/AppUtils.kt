package com.aikya.orchestrator.utils

import com.aikya.orchestrator.annotation.FieldLabel
import com.aikya.orchestrator.dto.common.ModelDashBoardTable
import com.aikya.orchestrator.repository.client.TableColumnInfo
import com.aikya.orchestrator.utils.AppConstants.ANSI_CYAN
import com.aikya.orchestrator.utils.AppConstants.ANSI_GREEN
import com.aikya.orchestrator.utils.AppConstants.ANSI_PINK
import com.aikya.orchestrator.utils.AppConstants.ANSI_PURPLE
import com.aikya.orchestrator.utils.AppConstants.ANSI_RED
import com.aikya.orchestrator.utils.AppConstants.ANSI_RESET
import com.aikya.orchestrator.utils.AppConstants.allClientEventsUIFlow
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.File
import java.io.ObjectInputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.sql.Blob
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*
import java.util.zip.GZIPInputStream
import javax.sql.rowset.serial.SerialBlob
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible


object AppUtils {
    private val logger: Logger = LoggerFactory.getLogger(AppUtils::class.java)
    private const val DATE_FORMAT_YYYYMMDD_HHMMSS = "yyyy-MM-dd HH:mm:ss"

    fun getUuid16(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16)
    }

    fun getUuid8(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 8)
    }
    fun getUuid4(): String {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 4)
    }

    fun getRandomLongNumber(min: Int, max: Int): Long {
        return Math.floor(Math.random() * (max - min + 1)).toLong() + min
    }

    fun getRandomId(): Long {
        return Math.floor(Math.random() * 9000000000L).toLong() + 1000000000L
    }

    fun randomName(length: Int): String {
        val numbers = "0123456789"
        val capitalChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val smallChars = "abcdefghijklmnopqrstuvwxyz"
        val sb = StringBuilder()
        val values = capitalChars + smallChars + numbers + smallChars
        val rndmMethod = Random()
        for (i in 0 until length) {
            if (i == 0) {
                sb.append(capitalChars[rndmMethod.nextInt(capitalChars.length)])
            } else {
                sb.append(values[rndmMethod.nextInt(values.length)])
            }
        }
        return sb.toString()
    }

    fun getCurrent(): Date? {
        return Timestamp.valueOf(LocalDateTime.now())
    }

    fun toCurrentDateString(): String {
        val format = SimpleDateFormat("yyyy-MM-dd-HHmmssSSS")
        return format.format(Date())
    }
    fun truncateTime(date: Date): Date {
        val calendar = Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.time
    }

    fun getDateFromNow(day: Int): Date? {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, -day)
        return cal.time
    }

    fun dateToYYYYMMDD(date: Date?): String? {
        val sdfAmerica = SimpleDateFormat("yyyy-MM-dd")
        return sdfAmerica.format(date)
    }
    fun dateToYYYYMMDDHHMMSS(date: Date?): String {
        if (date == null) {
            return ""
        }
        val sdfAmerica = SimpleDateFormat(DATE_FORMAT_YYYYMMDD_HHMMSS)
        return sdfAmerica.format(date)
    }
    fun dateToMMDDHHMM(date: Date): String {
        val sdfFormatted = SimpleDateFormat("MMM dd,HH:mm", Locale.US)
        return sdfFormatted.format(date)
    }
    fun dateToMMDD(date: Date): String {
        val sdfFormatted = SimpleDateFormat("MMM dd", Locale.US)
        return sdfFormatted.format(date)
    }
    fun getDateFromDatePoint(day: Int, cal: Calendar): Date? {
        cal.add(Calendar.DATE, -day)
        return cal.time
    }

    fun YYYYMMDDToCalendar(date: String?): Calendar? {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        cal.time = sdf.parse(date)
        return cal
    }

    fun getLastNDay(day: Int): String? {
        return dateToYYYYMMDD(getDateFromNow(day))
    }

    fun getDateFromDatePoint(range: Int, date: String?): String? {
        val pointCal = YYYYMMDDToCalendar(date)
        pointCal!!.add(Calendar.DATE, -range)
        return dateToYYYYMMDD(pointCal.time)
    }

    fun getLastNDayRange(from: Int, to: Int): Array<String?> {
        val data = arrayOfNulls<String>(2)
        val fromDay = getLastNDay(from)
        val endDay = getLastNDay(to)
        val fromDayTs = fromDay + " 00:00:00"
        val endDayTs = endDay + " 23:59:59"
        data[0] = fromDayTs
        data[1] = endDayTs
        return data
    }
    fun getFileName(fileName: String, modeType: String, version: Int): String {
        val file = File(fileName)
        val fileName = file.name.removeSuffix(".csv")
        val name = "$fileName-$modeType-V$version"
        return name
    }

    fun getLastNDay(day: Int, datePoint: String): String? {
        return dateToYYYYMMDD(getDateFromNow(day))
    }

    fun getLastNWeekRange(datePoint: String, range: Int): Array<String?> {
        val data = arrayOfNulls<String>(2)
        val datePointCal = YYYYMMDDToCalendar(datePoint)
        val prevDay = dateToYYYYMMDD(getDateFromDatePoint(range, datePointCal!!))
        val prevDayTs = prevDay + " 00:00:00"
        val datePointTs = datePoint + " 23:59:59"
        data[0] = prevDayTs
        data[1] = datePointTs
        return data
    }

    fun getDateTimestamp(timestampString: String?): Date {
        val formatter = SimpleDateFormat(DATE_FORMAT_YYYYMMDD_HHMMSS, Locale.ENGLISH)
        return formatter.parse(timestampString)
    }

    fun getDateTimestamp2(timestampString: String?): Date {
        val formatter = SimpleDateFormat("MM/dd/yy HH:mm", Locale.ENGLISH)
        return formatter.parse(timestampString)
    }

    fun getDateTimestamp3(timestampString: String?): Date {
        val formatter = SimpleDateFormat("MM/dd/yy HH:mm:ss", Locale.ENGLISH)
        return formatter.parse(timestampString)
    }
    fun convertStringToDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        return dateFormat.parse(dateString)
    }
    fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
        if (this != null) f(this)
    }
    fun convertToJsonString(obj: Any): String {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsString(obj)
    }
    fun encodeName(name: String, length: Int = 10): String {
        // Encode the name in Base64
        val encoded = Base64.getUrlEncoder().encodeToString(name.toByteArray())

        // Ensure the encoded string is exactly the desired length
        return if (encoded.length > length) {
            // Trim the string if it's too long
            encoded.substring(0, length)
        } else {
            // Pad the string with 'X' if it's too short
            encoded.padEnd(length, 'X')
        }
    }

    fun decodeName(encodedName: String): String {
        // Remove any padding characters
        val cleanedEncodedName = encodedName.trimEnd('X')

        // Decode the Base64 string back to the original name
        val decodedBytes = Base64.getUrlDecoder().decode(cleanedEncodedName)
        return String(decodedBytes)
    }
    fun generateWorkflowUUID(clientId: String, length: Int): String {
        return getUuid8() + "-" + encodeName(clientId, length) + "-" + getUuid4()
    }
    fun Boolean.toTFString(): String {
        return if (this) "T" else "F"
    }
    fun <T : Any> convertToKeyValue(entity: T): List<Map<String, Any?>> {
        return entity::class.memberProperties.map { property ->
            property.isAccessible = true
            try {
                mapOf("key" to property.name, "value" to property.getter.call(entity))
            } catch (e: Exception) {
                logger.error("Error accessing property '${property.name}' of ${entity::class.simpleName}", e)
                mapOf("key" to property.name, "value" to null)
            }
        }
    }
    fun snakeToCamel(snake: String): String {
        val parts = snake.split("_")
        return parts.first() + parts.drop(1).joinToString("") { part ->
            part.replaceFirstChar { it.uppercaseChar() }
        }
    }
    fun generateLabel(snake: String): String {
        return snake.split('_').joinToString(" ") { part ->
            part.replaceFirstChar { it.uppercaseChar() }
        }
    }
    fun convertQueryResultToKeyValueList(columns: List<TableColumnInfo>, result: Any): List<Map<String, Any?>> {
        val rowArray = if (result is Array<*>) result else return emptyList()
        return columns.zip(rowArray).map { (column, value) ->
            mapOf(
                "key" to snakeToCamel(column.columnName),
                "value" to value,
                "label" to generateLabel(column.columnName)
            )
        }
    }

    fun <T : Any> convertToBoardTable(itemList: List<T>): ModelDashBoardTable {
        if (itemList.isEmpty()) {
            return ModelDashBoardTable.empty()
        }

        val clazz = itemList[0]::class
        val headers = clazz.memberProperties.mapNotNull { property ->
            val annotation = property.findAnnotation<FieldLabel>()
            annotation?.let {
                Triple(property.name, it.label, it.order)
            }
        }.sortedBy { it.third }  // Sort by the order field

        val sortedProperties = headers.map { it.first }
        val headerMaps = headers.map { mapOf("field" to it.first, "label" to it.second) }
        val rows = itemList.map { item ->
            sortedProperties.map { propertyName ->
                clazz.memberProperties.find { it.name == propertyName }?.let { property ->
                    property.isAccessible = true
                    val value = property.getter.call(item)
                    when (value) {
                        is Date -> dateToYYYYMMDDHHMMSS(value)
                        is Double -> BigDecimal(value).setScale(2, RoundingMode.FLOOR).toPlainString()
                        else -> value
                    }
                }
            }
        }

        return ModelDashBoardTable(headerMaps, rows)
    }
    fun convertBlobToBase64String(blob: Blob?): String? {
        if (blob == null) {
            return null
        }
        val bytes = blob.getBytes(1, blob.length().toInt())
        return Base64.getEncoder().encodeToString(bytes)
    }
    fun isBlobNullOrEmpty(blob: Blob?): Boolean {
        return blob == null || blob.length() == 0L
    }
    fun convertBase64StringToByteArray(base64String: String): ByteArray {
        return Base64.getDecoder().decode(base64String)
    }
    fun convertByteArrayToBase64String(byteArray: ByteArray): String {
        return Base64.getEncoder().encodeToString(byteArray)
    }
    fun convertBase64StringToBlob(base64String: String): Blob? {
        return if (base64String.isBlank()) {
            SerialBlob(ByteArray(0)) // Returning an empty Blob
        } else {
            val byteArray = convertBase64StringToByteArray(base64String)
            SerialBlob(byteArray)
        }
    }
    fun getNStrings(input: String, num: Int): String {
        return input.take(num)
    }

    fun decompressWeights(encodedWeights: String): Any? {
        val decodedBytes = Base64.getDecoder().decode(encodedWeights)
        val gzipInputStream = GZIPInputStream(ByteArrayInputStream(decodedBytes))
        val objectInputStream = ObjectInputStream(gzipInputStream)
        return objectInputStream.readObject()
    }

    fun success(logger: Logger, message: String) {
        logger.info("$ANSI_GREEN$message$ANSI_RESET")
    }

    fun failure(logger: Logger, message: String) {
        logger.error("$ANSI_RED$message$ANSI_RESET")
    }

    fun display(logger: Logger, message: String) {
        logger.info("$ANSI_PINK$message$ANSI_RESET")
    }
    fun displayStep(logger: Logger, workflowTraceId: String, flowEvent: FlowEvent) {
        val step = flowEvent.step
        val event = flowEvent.event
        val label = flowEvent.label

        val formattedMessage = buildString {
            append("\n ${ANSI_CYAN}-- Workflow Trace ID: $workflowTraceId ${ANSI_CYAN} ${ANSI_RESET} ")
            append("${ANSI_GREEN}*** Step: $step${ANSI_GREEN} ***${ANSI_RESET}: | ")
            append("${ANSI_PURPLE}Event: $event${ANSI_RESET} " )
            append("${ANSI_PINK}Label: $label${ANSI_RESET} | ---")
        }
        display(logger, formattedMessage)
    }
    fun printPartialWeight(blob: Blob?, name: String, logger: Logger) {
        if (blob != null) {
            try {
                val blobBytes = blob.getBytes(1, blob.length().toInt())
                val lengthToPrint = (blobBytes.size * 0.2).toInt()
                val first20PercentBytes = blobBytes.sliceArray(0 until lengthToPrint)
                val first20PercentString = first20PercentBytes.joinToString("") { "%02x".format(it) }
                logger.info("weight data for $name: $first20PercentString")
            } catch (e: Exception) {
                logger.error("Error reading weight data for $name", e)
            }
        } else {
            logger.info("No data found for $name")
        }
    }
    fun findClientUIFlow(flowEvent: FlowEvent): UIFlowEvent {
        return allClientEventsUIFlow.find { uiFlow ->
            uiFlow.flowEvents.values.contains(flowEvent)
        } ?: throw IllegalArgumentException("No matching Client_UI_Flow found for the provided FlowEvent: ${flowEvent.event}")
    }

    fun parseStartOfDay(date: String): Long {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        val localDate = java.time.LocalDate.parse(date, formatter)
        return localDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun parseEndOfDay(date: String): Long {
        val formatter = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd")
        val localDate = java.time.LocalDate.parse(date, formatter)
        return localDate.atTime(23, 59, 59, 999_999_999).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}