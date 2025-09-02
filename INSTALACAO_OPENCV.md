# Instalação e Configuração do OpenCV para CPMA

## 📋 Visão Geral

Este documento explica como configurar o OpenCV para o sistema de identificação facial do CPMA. O OpenCV é uma biblioteca de visão computacional que permite processamento de imagens e reconhecimento facial.

## 🛠️ Dependências Necessárias

### 1. Java 24+

- Certifique-se de ter o Java 24 ou superior instalado
- Verifique com: `java -version`

### 2. Maven 3.6+

- Verifique com: `mvn -version`

### 3. OpenCV Native Libraries

- Windows: DLLs do OpenCV
- Linux: Bibliotecas compartilhadas (.so)
- macOS: Frameworks (.dylib)

## 🚀 Instalação no Windows

### Opção 1: Usando Maven (Recomendado)

O projeto já está configurado com as dependências do OpenCV no `pom.xml`:

```xml
<dependency>
    <groupId>org.openpnp</groupId>
    <artifactId>opencv</artifactId>
    <version>4.8.1-0</version>
</dependency>
<dependency>
    <groupId>org.bytedeco</groupId>
    <artifactId>javacv-platform</artifactId>
    <version>1.5.9</version>
</dependency>
```

### Opção 2: Instalação Manual

1. **Baixar OpenCV**

   - Acesse: https://opencv.org/releases/
   - Baixe a versão 4.8.1 para Windows

2. **Extrair e Configurar**

   ```bash
   # Extrair para C:\opencv
   # Adicionar ao PATH: C:\opencv\build\x64\vc15\bin
   ```

3. **Configurar Variáveis de Ambiente**
   ```bash
   OPENCV_DIR=C:\opencv\build
   PATH=%PATH%;%OPENCV_DIR%\x64\vc15\bin
   ```

## 🐧 Instalação no Linux

### Ubuntu/Debian

```bash
# Instalar dependências
sudo apt-get update
sudo apt-get install libopencv-dev python3-opencv

# Verificar instalação
pkg-config --modversion opencv4
```

### CentOS/RHEL

```bash
# Instalar dependências
sudo yum install opencv-devel

# Verificar instalação
pkg-config --modversion opencv
```

## 🍎 Instalação no macOS

### Usando Homebrew

```bash
# Instalar OpenCV
brew install opencv

# Verificar instalação
pkg-config --modversion opencv
```

### Usando MacPorts

```bash
# Instalar OpenCV
sudo port install opencv

# Verificar instalação
pkg-config --modversion opencv
```

## 🔧 Configuração do Projeto

### 1. Verificar Dependências

```bash
# Limpar e baixar dependências
mvn clean dependency:resolve
```

### 2. Compilar o Projeto

```bash
# Compilar
mvn clean compile

# Verificar se não há erros
mvn verify
```

### 3. Executar Testes

```bash
# Executar testes unitários
mvn test

# Executar aplicação
mvn javafx:run
```

## 🧪 Testando a Instalação

### 1. Teste Básico

```java
// Criar uma classe de teste simples
public class TesteOpenCV {
    public static void main(String[] args) {
        try {
            // Tentar carregar OpenCV
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            System.out.println("OpenCV carregado com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao carregar OpenCV: " + e.getMessage());
        }
    }
}
```

### 2. Teste de Funcionalidade

```bash
# Executar teste
mvn exec:java -Dexec.mainClass="TesteOpenCV"
```

## 🚨 Solução de Problemas

### Erro: "UnsatisfiedLinkError"

- **Causa**: Bibliotecas nativas não encontradas
- **Solução**: Verificar PATH e variáveis de ambiente

### Erro: "NoClassDefFoundError"

- **Causa**: Dependências Maven não baixadas
- **Solução**: Executar `mvn dependency:resolve`

### Erro: "Library not found"

- **Causa**: OpenCV não instalado ou não no PATH
- **Solução**: Reinstalar OpenCV e configurar PATH

## 📱 Funcionalidades Disponíveis

### 1. Detecção de Faces

- Usa classificadores Haar Cascade
- Detecta múltiplas faces na imagem
- Retorna coordenadas das faces detectadas

### 2. Extração de Descritores

- Converte imagem para vetor de características
- Normaliza e redimensiona automaticamente
- Armazena como JSON no banco de dados

### 3. Comparação Facial

- Algoritmo de similaridade baseado em correlação
- Threshold configurável para precisão
- Suporte para múltiplas imagens por usuário

## 🔮 Melhorias Futuras

### 1. Algoritmos Avançados

- Deep Learning para reconhecimento facial
- FaceNet ou similar para descritores mais precisos
- Suporte para detecção de emoções

### 2. Performance

- Processamento em paralelo
- Cache de descritores em memória
- Otimização de algoritmos de comparação

### 3. Interface

- Captura em tempo real da webcam
- Preview da detecção facial
- Histórico de identificações

## 📞 Suporte

Para problemas específicos:

1. **Verificar logs**: `mvn clean compile -X`
2. **Verificar dependências**: `mvn dependency:tree`
3. **Testar OpenCV**: Executar classe de teste
4. **Verificar PATH**: Variáveis de ambiente

## 📚 Recursos Adicionais

- [Documentação OpenCV](https://docs.opencv.org/)
- [JavaCV GitHub](https://github.com/bytedeco/javacv)
- [OpenCV Tutorials](https://docs.opencv.org/master/d9/df8/tutorial_root.html)
- [Maven Central](https://search.maven.org/)

---

**Nota**: Esta implementação usa uma versão simplificada do OpenCV para demonstração. Para produção, considere usar algoritmos mais avançados como FaceNet ou DeepFace.
