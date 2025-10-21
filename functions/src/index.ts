import * as admin from "firebase-admin";
import {onSchedule} from "firebase-functions/v2/scheduler";
import * as logger from "firebase-functions/logger";

admin.initializeApp();

const DB_NODE = "Fianza";

/**
 * Normaliza un epoch (segundos o milisegundos) a milisegundos.
 *
 * @param {unknown} value - valor numÃ©rico o string con epoch.
 * @return {number} epoch en milisegundos (0 si invÃ¡lido).
 */
function normalizeEpochToMs(value: unknown): number {
  if (value == null) return 0;
  const n = typeof value === "string" ? Number(value) : (value as number);
  if (Number.isNaN(Number(n))) return 0;
  return Number(n) < 1_000_000_000_000 ? Number(n) * 1000 : Number(n);
}

/**
 * FunciÃ³n programada: busca fianzas con estado "1" cuya
 * fechaNotificacion cae en el dÃ­a actual y notifica.
 */
export const notificarFianzasPorVencer = onSchedule(
  {schedule: "0 9 * * *", timeZone: "America/Guatemala"},
  async (): Promise<void> => {
    logger.info("Iniciando verificaciÃ³n de fianzas por vencer...");

    const now = new Date();
    const startOfDay = new Date(
      now.getFullYear(),
      now.getMonth(),
      now.getDate(),
      0,
      0,
      0,
      0
    ).getTime();

    const endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1;
    const ref = admin.database().ref(DB_NODE);

    try {
      const snapshot = await ref
        .orderByChild("estado")
        .equalTo("1")
        .once("value");

      if (!snapshot.exists()) {
        logger.info("No hay fianzas con estado '1' para revisar.");
        return;
      }

      const pendientes: { id: string; datos: Record<string, unknown> }[] = [];
      const updates: Record<string, unknown> = {};

      snapshot.forEach((child: admin.database.DataSnapshot) => {
        const id = child.key;
        const datos = child.val() as Record<string, unknown>;
        if (!id || !datos) return;

        const fnRaw = datos.fechaNotificacion;
        const fnMs = normalizeEpochToMs(fnRaw);

        if (fnMs >= startOfDay && fnMs <= endOfDay) {
          pendientes.push({id, datos});
          updates[`${DB_NODE}/${id}/estado`] = "2";
        }
      });

      if (pendientes.length === 0) {
        logger.info("No se encontraron fianzas para notificar hoy.");
        return;
      }

      logger.info(`Se encontraron ${pendientes.length} fianza(s).`);

      let titulo = "Fianza por vencer";
      let cuerpo = "Hay varias fianzas prÃ³ximas a vencer. Revisa la app.";

      if (pendientes.length === 1) {
        const nombreProyecto =
          (pendientes[0].datos.nombreProyecto as string) || "";
        const nog = (pendientes[0].datos.nog as string) || "";
        titulo = "Fianza por vencer";
        cuerpo = `Fianza: ${nombreProyecto || nog || "Proyecto"}`;
      }

      const message: admin.messaging.Message = {
        notification: {title: titulo, body: cuerpo},
        android: {
          priority: "high",
          notification: {
            channelId: "fianzas_vencimiento",
            sound: "default",
          },
        },
        topic: "gestores",
        data: {
          type: "fianzas_notificacion",
          fianzasIds: pendientes.map((p) => p.id).join(","),
        },
      };

      try {
        const messageId = await admin.messaging().send(message);
        logger.info("NotificaciÃ³n enviada. messageId:", messageId);
      } catch (sendErr) {
        logger.error("Error enviando FCM:", sendErr);
      }

      await admin.database().ref().update(updates);
      logger.info("Estados actualizados a '2' para fianzas notificados.");
      return;
    } catch (err) {
      logger.error("Error en funciÃ³n notificarFianzasPorVencer:", err);
      return;
    }
  }
);
