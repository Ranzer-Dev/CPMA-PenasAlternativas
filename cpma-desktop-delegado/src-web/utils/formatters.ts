/**
 * Utility functions for formatting, validation, and calculations
 * Matching Java classes ValidadorCPF, FormatacaoUtils, CodigoPenaUtil, and HashUtil.
 */

// Format CPF with 000.000.000-00 mask
export function formatarCPF(cpf: string): string {
  if (!cpf) return '';
  const digits = cpf.replace(/\D/g, '').slice(0, 11);
  if (digits.length <= 3) return digits;
  if (digits.length <= 6) return `${digits.slice(0, 3)}.${digits.slice(3)}`;
  if (digits.length <= 9) return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6)}`;
  return `${digits.slice(0, 3)}.${digits.slice(3, 6)}.${digits.slice(6, 9)}-${digits.slice(9, 11)}`;
}

// Clean CPF leaving only numbers
export function limparCPF(cpf: string): string {
  return (cpf || '').replace(/\D/g, '');
}

// Validates CPF using official check digits algorithm (matching ValidadorCPF.java)
export function validarCPF(cpf: string): boolean {
  const digits = limparCPF(cpf);
  if (digits.length !== 11) return false;
  if (/^(\d)\1{10}$/.test(digits)) return false; // Repeated digits like 111.111.111-11

  // 1st digit verification
  let sum = 0;
  for (let i = 0; i < 9; i++) {
    sum += parseInt(digits.charAt(i), 10) * (10 - i);
  }
  let rev = 11 - (sum % 11);
  if (rev === 10 || rev === 11) rev = 0;
  if (rev !== parseInt(digits.charAt(9), 10)) return false;

  // 2nd digit verification
  sum = 0;
  for (let i = 0; i < 10; i++) {
    sum += parseInt(digits.charAt(i), 10) * (11 - i);
  }
  rev = 11 - (sum % 11);
  if (rev === 10 || rev === 11) rev = 0;
  return rev === parseInt(digits.charAt(10), 10);
}

// Format Phone (00) 00000-0000
export function formatarTelefone(telefone: string): string {
  if (!telefone) return '';
  const digits = telefone.replace(/\D/g, '').slice(0, 11);
  if (digits.length <= 2) return `(${digits}`;
  if (digits.length <= 6) return `(${digits.slice(0, 2)}) ${digits.slice(2)}`;
  if (digits.length <= 10) return `(${digits.slice(0, 2)}) ${digits.slice(2, 6)}-${digits.slice(6)}`;
  return `(${digits.slice(0, 2)}) ${digits.slice(2, 7)}-${digits.slice(7, 11)}`;
}

// Format CEP 00000-000
export function formatarCEP(cep: string): string {
  if (!cep) return '';
  const digits = cep.replace(/\D/g, '').slice(0, 8);
  if (digits.length <= 5) return digits;
  return `${digits.slice(0, 5)}-${digits.slice(5)}`;
}

// Format Date YYYY-MM-DD -> DD/MM/YYYY
export function formatarData(dataStr?: string): string {
  if (!dataStr) return '-';
  const parts = dataStr.split('T')[0].split('-');
  if (parts.length === 3) {
    return `${parts[2]}/${parts[1]}/${parts[0]}`;
  }
  return dataStr;
}

// Format Time HH:mm
export function formatarHora(hora?: string): string {
  if (!hora) return '--:--';
  return hora.slice(0, 5);
}

// Compute hours worked between times (handles lunch break)
export function calcularHorasTrabalhadas(
  inicio?: string,
  almoco?: string,
  volta?: string,
  saida?: string
): number {
  if (!inicio || !saida) return 0;
  const toMin = (t: string) => {
    const [h, m] = t.split(':').map(Number);
    return (h || 0) * 60 + (m || 0);
  };
  const minInicio = toMin(inicio);
  const minSaida = toMin(saida);

  let totalMin = minSaida - minInicio;
  if (almoco && volta) {
    const minAlmoco = toMin(almoco);
    const minVolta = toMin(volta);
    if (minVolta > minAlmoco) {
      totalMin -= (minVolta - minAlmoco);
    }
  }

  return Math.max(0, Math.round((totalMin / 60) * 10) / 10);
}
