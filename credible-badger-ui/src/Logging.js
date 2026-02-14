export function logInfo(data) {
    if (import.meta.env.PROD) {
        return;
    }
    console.log(data);
}

export function logError(data) {
    if (import.meta.env.PROD) {
        return;
    }
    console.error(data);
}