export function getApiError(error, fallback = 'Request failed') {
  const payload = error?.response?.data
  return {
    message: payload?.message ?? fallback,
    code: payload?.errorCode ?? null,
    traceId: payload?.traceId ?? null,
  }
}

export function getApiErrorMessage(error, fallback = 'Request failed') {
  const { message, code, traceId } = getApiError(error, fallback)
  const suffix = [code, traceId ? `Trace: ${traceId}` : null].filter(Boolean).join(' | ')
  return suffix ? `${message} (${suffix})` : message
}
