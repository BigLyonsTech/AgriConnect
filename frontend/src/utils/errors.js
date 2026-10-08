/**
 * Extracts a human-readable message from an axios error, matching the
 * shape returned by the backend's GlobalExceptionHandler:
 *   { timestamp, status, error, message, fieldErrors? }
 */
export function extractErrorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data;
  if (!data) return fallback;

  if (data.fieldErrors && typeof data.fieldErrors === 'object') {
    const firstField = Object.values(data.fieldErrors)[0];
    if (firstField) return firstField;
  }

  return data.message || fallback;
}
