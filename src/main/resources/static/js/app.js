/*
 * Pastelería X7 — JavaScript de interfaz.
 *
 * Todos los datos (usuarios, productos y pedidos) los renderiza Thymeleaf en el
 * servidor a partir de la base de datos. Este archivo solo contiene mejoras de
 * interfaz que no dependen de los datos: filtros del catálogo, cálculo
 * orientativo del total, confirmaciones y vista previa de imágenes.
 */

document.addEventListener("DOMContentLoaded", function () {
  filtrarCatalogo();
  estimarTotalPedido();
  autoenviarSelects();
  previsualizarImagen();
});

/* ---------- Confirmación antes de una acción destructiva ---------- */
document.addEventListener("click", function (evento) {
  const disparador = evento.target.closest("[data-confirmar]");
  if (disparador && !window.confirm(disparador.dataset.confirmar)) {
    evento.preventDefault();
  }
});

/* ---------- Catálogo: búsqueda y filtro por categoría ---------- */
function filtrarCatalogo() {
  const grilla = document.getElementById("grillaProductos");
  if (!grilla) return;

  const tarjetas = Array.from(grilla.querySelectorAll(".producto-card"));
  const buscador = document.getElementById("buscadorProductos");
  const sinResultados = document.getElementById("sinResultados");
  const botones = Array.from(document.querySelectorAll("[data-categoria]"));
  let categoriaActiva = "";

  function aplicar() {
    const texto = buscador.value.trim().toLowerCase();
    let visibles = 0;

    tarjetas.forEach(function (tarjeta) {
      const categoria = tarjeta.dataset.categoria || "";
      const contenido = (tarjeta.dataset.texto || "").toLowerCase();
      const coincide =
        (!categoriaActiva || categoria === categoriaActiva) &&
        (!texto || contenido.includes(texto));

      tarjeta.classList.toggle("d-none", !coincide);
      if (coincide) visibles++;
    });

    if (sinResultados) {
      sinResultados.classList.toggle("d-none", visibles > 0 || tarjetas.length === 0);
    }
  }

  botones.forEach(function (boton) {
    boton.addEventListener("click", function () {
      categoriaActiva = boton.dataset.categoria;
      botones.forEach(function (otro) {
        otro.classList.remove("btn-terracotta");
        otro.classList.add("btn-outline-burgundy");
      });
      boton.classList.remove("btn-outline-burgundy");
      boton.classList.add("btn-terracotta");
      aplicar();
    });
  });

  buscador.addEventListener("input", aplicar);
}

/* ---------- Pedido: total estimado (el monto real lo calcula el servidor) ---------- */
function estimarTotalPedido() {
  const selectProducto = document.getElementById("producto");
  const selectTamano = document.getElementById("tamano");
  const inputCantidad = document.getElementById("cantidad");
  const salida = document.getElementById("totalEstimado");

  if (!selectProducto || !selectTamano || !inputCantidad || !salida) return;

  function calcular() {
    const opcionProducto = selectProducto.selectedOptions[0];
    const opcionTamano = selectTamano.selectedOptions[0];

    const precio = Number(opcionProducto && opcionProducto.dataset.precio);
    const factor = Number(opcionTamano && opcionTamano.dataset.factor);
    const cantidad = Number(inputCantidad.value) || 1;

    salida.value =
      precio > 0 && factor > 0
        ? "S/ " + (precio * factor * cantidad).toFixed(2)
        : "Por confirmar";
  }

  [selectProducto, selectTamano, inputCantidad].forEach(function (campo) {
    campo.addEventListener("change", calcular);
    campo.addEventListener("input", calcular);
  });

  calcular();
}

/* ---------- Envía el formulario al cambiar el select (cambio de estado) ---------- */
function autoenviarSelects() {
  document.querySelectorAll(".js-auto-submit").forEach(function (select) {
    select.addEventListener("change", function () {
      select.form.submit();
    });
  });
}

/* ---------- Vista previa de la imagen del producto ---------- */
function previsualizarImagen() {
  const campo = document.querySelector(".js-preview-origen");
  const contenedor = document.getElementById("previewWrapper");
  const imagen = document.getElementById("previewImagen");

  if (!campo || !contenedor || !imagen) return;

  campo.addEventListener("input", function () {
    const url = campo.value.trim();
    imagen.src = url;
    contenedor.classList.toggle("d-none", url === "");
  });
}
